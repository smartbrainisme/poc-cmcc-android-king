package com.airtalkee.tts;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import com.airtalkee.config.Config;
import com.airtalkee.receiver.ReceiverNoScreenOper;
import com.sinovoice.hcicloudsdk.android.tts.player.TTSPlayer;
import com.sinovoice.hcicloudsdk.api.HciCloudSys;
import com.sinovoice.hcicloudsdk.common.AuthExpireTime;
import com.sinovoice.hcicloudsdk.common.HciErrorCode;
import com.sinovoice.hcicloudsdk.common.InitParam;
import com.sinovoice.hcicloudsdk.common.asr.AsrInitParam;
import com.sinovoice.hcicloudsdk.common.hwr.HwrInitParam;
import com.sinovoice.hcicloudsdk.common.tts.TtsConfig;
import com.sinovoice.hcicloudsdk.common.tts.TtsInitParam;
import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer;
import com.sinovoice.hcicloudsdk.player.TTSCommonPlayer.PlayerEvent;
import com.sinovoice.hcicloudsdk.player.TTSPlayerListener;

public class TTSManager  {
	private Context context = null;
	public static final String TAG = "ExampleActivity";

	/** Called when the activity is first created. */
	private TtsConfig ttsConfig = null;
	private TTSPlayer m = null;
	private Map<String, String> mAccountInfo = null;
	String dataPath = null;
	// 路径
	public final String SDCARD_PATH = Environment.getExternalStorageDirectory()
			.getAbsolutePath();
	private static final String COMPONEY_FOLDER = "HciCloudExample";
	private static final String APP_FOLDER = "HciCloudExample";
	private static final String LOG_FOLDER = "log";
	private static final String FOLDER_SEP = File.separator;

	private TTSManager (){}
	
	private static TTSManager instance = null;
	
	public static TTSManager getInstance()
	{
		if(instance == null)
		{
			instance = new TTSManager();
		}
		return instance;
	}
	public void init(Context context) {
		if(!Config.funcTTS)
			return;
		 this.context = context;
		 System.loadLibrary("LedCustom");
		 String sdcardState = Environment.getExternalStorageState();
		 if (Environment.MEDIA_MOUNTED.equals(sdcardState)) 
		 {
	            String sdPath = Environment.getExternalStorageDirectory()  .getAbsolutePath();
		    String dataPath = sdPath + File.separator + "AirTalkee" + File.separator + "data" + File.separator;
			this.dataPath = dataPath;
		 }
		 else
		 {
			 com.airtalkee.sdk.util.Log.d(TTSManager.class, "dataPath error");
		 }
		// 获取账号信息
		mAccountInfo = new HashMap<String, String>();

		/*
		 * 从文件assets/AccountInfo.txt获取从捷通分配的应用账号信息
		 * strAccountInfo形如："appKey=##,developerKey=###,cloudUrl=###"
		 * 用户实际使用时可直接使用一个字符串代替 *
		 */
		boolean isRet = GetAccountInfo();
		if (!isRet) {
			return;
		}
		// 加载信息,返回InitParam, 获得配置参数的字符串
		/*
		 * 系统初始化 辅助工具 InitParam: 该类的实例通过addParam(key, value)的方式添加用户账户信息,
		 * 再通过getStringConfig() 获取初始化时需要的字符串参数 初始化方法:
		 * HciCloudSys.hciInit(config, context), 该方法需要传入初始化配置参数(由InitParam生成)以及
		 * Android中的上下文Context的实例
		 */
		InitParam initParam = getInitParam(context);
		String strConfig = initParam.getStringConfig();
		Log.i(TAG, "strConfig value:" + strConfig);
        //灵云系统初始化
		int nRet = HciCloudSys.hciInit(strConfig, context);
		if (nRet != HciErrorCode.HCI_ERR_NONE) {
			Log.e(TAG, "hciInit error: " + nRet);
			Toast.makeText(context, "系统初始化失败", Toast.LENGTH_LONG).show();
			return;
		} else {
			Log.i(TAG, "hciInit success");
		}
		/*
		 * 获取授权/更新授权文件 : 更新授权文件有如下两种做法： 1 在设置hciInit的参数时将 autoCloudAuth 配置项设为
		 * yes，系统会启动一个后台线程，定期检查授权的过期时间， 如果授权过期时间快到时，会自动更新授权文件。此选项亦为缺省配置。 2 如果将
		 * autoCloudAuth 设为 no，则需要开发者自行在适当的时候通过调用hciCheckAuth()来更新授权。
		 * 自动更新授权的方式(方式1)在PC等联网条件不受限的情况下会工作得很好，但对于移动终端应用等对网络条件和流量比较敏感的情况来说，
		 * 最好由开发者自行决定更新授权的时机(方式2)，这样可控性更强，例如可以在WiFi打开的时候才进行授权更新等等。 开发者可以使用
		 * hciGetAuthExpireTime() 获取当前授权的过期时间，当此方法返回错误或者授权过期时间已经快到了的时候， 再调用
		 * hciCheckAuth()函数到云端下载授权文件。 下面的示例会在授权过期前的一周内，开始更新授权。
		 */
		boolean checkAuthSuccess = checkAuth();
		if (!checkAuthSuccess) {
			// 由于系统已经初始化成功,在结束前需要调用方法hciRelease()进行系统的反初始化
			HciCloudSys.hciRelease();
			Toast.makeText(context, "获取授权失败", Toast.LENGTH_LONG).show();
			return;
		}
        //传入了capKey初始化TTS播发器
		boolean isPlayerInitSuccess = initPlayer();
		if (!isPlayerInitSuccess) {
			Toast.makeText(context, "播放器初始化失败", Toast.LENGTH_LONG).show();
			return;
		}
		
		ReceiverNoScreenOper.getInstance().receiveReigster();
	}

	public void destroy() {
		if (m != null) {
			m.release();
			HciCloudSys.hciRelease();
		}
	}

	// //////////////////////////////////////////////
	// ////
	// //// 灵云能力初始化相关
	// ////
	// //////////////////////////////////////////////
	/**
	 * 初始化播放器
	 */
	private boolean initPlayer() {
		m = new TTSPlayer();

		// 配置TTS初始化参数
		ttsConfig = new TtsConfig();
		m.init(getTtsInitParam().getStringConfig(),
				new TTSEventProcess());

		if (m.getPlayerState() == TTSPlayer.PLAYER_STATE_IDLE) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取TTS初始化参数
	 * 
	 * @return
	 */
	private TtsInitParam getTtsInitParam() {
		/*
		 * Tts引擎初始化 辅助工具 : TtsInitParam:该类的实例通过addParam(key,
		 * value)的方式添加Tts初始化的参数, 再通过getStringConfig() 获取初始化时需要的字符串参数 config
		 * 初始化方法: HciCloudTts.hciTtsInit(config)
		 */
		// 构造Tts初始化的帮助类的实例
		TtsInitParam ttsInitParam = new TtsInitParam();

		// Tts引擎初始化时主要需要传入 PARAM_KEY_DATA_PATH, PARAM_KEY_INIT_CAP_KEYS,
		// PARAM_KEY_FILE_FLAG三个参数
		// 其中参数PARAM_KEY_DATA_PATH 是指Tts依赖的资源库文件的路径,习惯上在Android中应将资源库文件命名
		// 为libLibraryName.so的形式并放到工程中的libs文件夹下;
		// 也可以将资源文件放到SD卡的固定目录下并将路径asrDirPath
		// 作为参数PARAM_KEY_DATA_PATH 的值传给引擎, 此时资源文件可以不必命名为lib开头 .so结尾的方式;
		// 值得注意的是: 资源文件的名称不可以自定义, 如Tts依赖的资源文件ENG_Barron.n6.voclib,
		// 可以将其放置到SD卡的某路径下, 也可以
		// 命名为libENG_Barron.n6.voclib.so后放置到libs中,
		// 但ENG_Barron.n6.voclib这个名称是不可以随意修改的, 否则引擎会报无法找到
		// 资源文件的错误
		// 参数PARAM_KEY_INIT_CAP_KEYS 是指使用的Tts的能力,
		// 该能力的值是一个型为"tts.cloud.xiaokun"的字符串, 具体到用户的帐号拥有
		// 哪些可以使用的能力, 请资讯捷通公司的客户人员
		// 参数PARAM_KEY_FILE_FLAG 是指当前资源文件的类型标志, 如用户选择
		// libLibraryName.so的形式命名资源文件, 应将标志设置为
		// "android_so", 否则应设置为"none"或者缺省
		
		ttsInitParam.addParam(TtsInitParam.PARAM_KEY_DATA_PATH, dataPath);
		// 此处演示初始化的能力为tts.cloud.xiaokun, 用户可以根据自己可用的能力进行设置, 另外,此处可以传入多个能力值,并用;隔开
		// 如 "tts.cloud.xiaokun;tts.cloud.zhangNan;tts.local.xiaoKun"
		ttsInitParam.addParam(AsrInitParam.PARAM_KEY_INIT_CAP_KEYS,
				mAccountInfo.get("capKey"));
		// 使用lib下的资源文件,需要添加android_so的标记
		ttsInitParam.addParam(HwrInitParam.PARAM_KEY_FILE_FLAG, "none");

		return ttsInitParam;
	}

	private boolean GetAccountInfo() {
		// 加载用户的初始化信息,平台id,开发者id等等
		// 用户应用自己的信息将asset文件夹中的文件AccountInfo.txt填充完整
		try {
			loadAccountInfo("AccountInfo.txt");
		} catch (IOException e) {
			e.printStackTrace();
			// 读取错误,通知界面并返回
			Log.e(TAG, "load account info error");
			return false;
		}
		return true;
	}

	/**
	 * 获取授权
	 * 
	 * @return true 成功
	 */
	private boolean checkAuth() {
		// 获取系统授权到期时间
		// AuthExpireTime 是保存引擎返回授权日期的帮助类,其方法getExpireTime() 返回一个返回了一个long型数据,
		// 返回授权的结束日期距离1970,1,1 0:0:0的秒数(注:区别Java,此处返回值为秒数,而非毫秒数)
		// hciGetAuthExpireTime()为native方法,需要传入的对象为AuthExpireTime的实例;
		// 返回值为错误码:
		// 如果为HCI_ERR_NONE(0) 则表示获取授权日期成功,否则请根据SDK帮助文档中"常量字段值"中的
		// 错误码的含义检查错误所在
		int nRet;
		AuthExpireTime objExpireTime = new AuthExpireTime();
		nRet = HciCloudSys.hciGetAuthExpireTime(objExpireTime);
		if (nRet == HciErrorCode.HCI_ERR_NONE) {

			// 显示授权日期,如用户不需要关注该值,此处代码可忽略
			Date date = new Date(objExpireTime.getExpireTime() * 1000);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.CHINA);
			Log.i(TAG, "expire time: " + sdf.format(date));

			if (objExpireTime.getExpireTime()*1000 < System.currentTimeMillis()) {
				// 获取授权方法, 返回值为错误码:
				// 如果为HCI_ERR_NONE(0) 则表示获取授权日期成功,否则请根据SDK帮助文档中 "常量字段值" 中的
				// 错误码的含义检查错误所在
				Log.i(TAG, "expired date");

				nRet = HciCloudSys.hciCheckAuth();
				if (nRet == HciErrorCode.HCI_ERR_NONE) {
					Log.i(TAG, "checkAuth success");
					return true;
				} else {
					Log.e(TAG, "checkAuth failed: " + nRet);
					return false;
				}
			} else {
				// 已经成功获取了授权,并且距离授权到期有充足的时间(>7天)
				Log.i(TAG, "checkAuth success");
				return true;
			}

			// 如果读取Auth文件失败(比如第一次运行,还没有授权文件),则开始获取授权
		} else if (nRet == HciErrorCode.HCI_ERR_SYS_AUTHFILE_INVALID) {
			Log.i(TAG, "authfile invalid");

			nRet = HciCloudSys.hciCheckAuth();
			if (nRet == HciErrorCode.HCI_ERR_NONE) {
				Log.i(TAG, "checkAuth success");
				return true;
			} else {
				Log.e(TAG, "checkAuth failed: " + nRet);
				return false;
			}
		} else {
			// 其他失败原因,请根据SDK帮助文档中"常量字段值"中的错误码的含义检查错误所在
			Log.e(TAG, "getAuthExpireTime Error:" + nRet);
			return false;
		}
	}

	/**
	 * 加载初始化信息
	 * 
	 * @param context
	 *            上下文语境
	 * @return 系统初始化参数
	 */
	private InitParam getInitParam(Context context) {

		String authDirPath = context.getFilesDir().getAbsolutePath();
		new File(authDirPath, "test"); // 保证文件夹已经被创建

		// 前置条件：无
		InitParam initparam = new InitParam();
		// 授权文件所在路径，此项必填
		initparam.addParam(InitParam.PARAM_KEY_AUTH_PATH, authDirPath);
		// 是否自动访问云授权,详见 获取授权/更新授权文件处注释
		initparam.addParam(InitParam.PARAM_KEY_AUTO_CLOUD_AUTH, "no");
		// 灵云云服务的接口地址，此项必填
		initparam.addParam(InitParam.PARAM_KEY_CLOUD_URL,
				mAccountInfo.get("cloudUrl"));
		// 开发者Key，此项必填，由捷通华声提供
		initparam.addParam(InitParam.PARAM_KEY_DEVELOPER_KEY,
				mAccountInfo.get("developerKey"));
		// 应用Key，此项必填，由捷通华声提供
		initparam.addParam(InitParam.PARAM_KEY_APP_KEY,
				mAccountInfo.get("appKey"));

		// 配置日志参数
		String sdcardState = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(sdcardState)) {
			// 日志文件地址
			String logDirPath = SDCARD_PATH + FOLDER_SEP + COMPONEY_FOLDER
					+ FOLDER_SEP + APP_FOLDER + FOLDER_SEP + LOG_FOLDER;
			File fileDir = new File(logDirPath);
			if (!fileDir.exists()) {
				fileDir.mkdirs();
			}
			// 日志的路径，可选，如果不传或者为空则不生成日志
			initparam.addParam(InitParam.PARAM_KEY_LOG_FILE_PATH, logDirPath);
			// 日志数目，默认保留多少个日志文件，超过则覆盖最旧的日志
			initparam.addParam(InitParam.PARAM_KEY_LOG_FILE_COUNT, "5");
			// 日志大小，默认一个日志文件写多大，单位为K
			initparam.addParam(InitParam.PARAM_KEY_LOG_FILE_SIZE, "1024");
			// 日志等级，0=无，1=错误，2=警告，3=信息，4=细节，5=调试，SDK将输出小于等于logLevel的日志信息
			initparam.addParam(InitParam.PARAM_KEY_LOG_LEVEL, "5");
		}

		return initparam;
	}

	/**
	 * 加载用户的注册信息
	 * 
	 * @param fileName
	 * @throws IOException
	 */
	private void loadAccountInfo(String fileName) throws IOException {
		InputStream in = null;
		in = context.getResources().getAssets().open(fileName);
		InputStreamReader inputStreamReader = new InputStreamReader(in, "gbk");
		BufferedReader br = new BufferedReader(inputStreamReader);
		String temp = null;
		String[] sInfo = new String[2];
		temp = br.readLine();
		while (temp != null) {
			if (!temp.startsWith("#") && !temp.equalsIgnoreCase("")) {
				sInfo = temp.split("=");
				if (sInfo.length == 2)
					mAccountInfo.put(sInfo[0], sInfo[1]);
			}
			temp = br.readLine();
		}
	}


	// 云端合成,不启用编码传输(默认encode=none)
	public void synth(String text) {
		if(!Config.funcTTS)
			return;
		// 配置播放器的属性。包括：音频格式，音库文件，语音风格，语速等等。详情见文档。
		ttsConfig = new TtsConfig();
		// 音频格式
		ttsConfig.addParam(TtsConfig.PARAM_KEY_AUDIO_FORMAT, "pcm16k16bit");
		// 指定语音合成的能力(云端合成,发言人是XiaoKun)
		ttsConfig.addParam(TtsConfig.PARAM_KEY_CAP_KEY,
				mAccountInfo.get("capKey"));
		// 设置合成语速
		ttsConfig.addParam(TtsConfig.PARAM_KEY_SPEED, "5");

		if (m.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PLAYING
				|| m.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_PAUSE) {
			m.stop();
		}

		if (m.getPlayerState() == TTSCommonPlayer.PLAYER_STATE_IDLE) {
			m.play(text,
					ttsConfig.getStringConfig());
		} else {
			Toast.makeText(context, "播放器内部状态错误",
					Toast.LENGTH_SHORT).show();
		}
	}

	// 播放器回调
	private class TTSEventProcess implements TTSPlayerListener {

		@Override
		public void onPlayerEventPlayerError(PlayerEvent playerEvent,
				int errorCode) {
			Log.i(TAG, "onError " + playerEvent.name() + " code: " + errorCode);
		}

		@Override
		public void onPlayerEventProgressChange(PlayerEvent playerEvent,
				int start, int end) {
			Log.i(TAG, "onProcessChange " + playerEvent.name() + " from "
					+ start + " to " + end);
		}

		@Override
		public void onPlayerEventStateChange(PlayerEvent playerEvent) {
			Log.i(TAG, "onStateChange " + playerEvent.name());
		}

	}
	
	
}
