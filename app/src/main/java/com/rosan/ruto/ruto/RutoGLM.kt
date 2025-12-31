package com.rosan.ruto.ruto

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.Display
import com.rosan.ruto.bigmodel.BigModel
import com.rosan.ruto.bigmodel.BigModelSession
import com.rosan.ruto.device.repo.DeviceRepo
import com.rosan.ruto.retrofit.RetrofitHttpClientBuilderFactory
import com.rosan.ruto.ruto.script.RutoRuntime
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.kotlin.model.chat.StreamingChatModelReply
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

val taskPrompt = """
你是一个智能体分析专家，可以根据操作历史和当前状态图执行一系列操作来完成任务。
你必须严格按照要求输出以下格式：
<think>选择这个操作的简短推理说明</think>
<answer>指令</answer>

操作指令集及其作用如下：
- do(action="Launch", app="xxx")  
    Launch是启动目标app的操作，这比通过主屏幕导航更快。此操作完成后，您将自动收到结果状态的截图。
- do(action="Tap", element=[x,y])  
    Tap是点击操作，点击屏幕上的特定点。可用此操作点击按钮、选择项目、从主屏幕打开应用程序，或与任何可点击的用户界面元素进行交互。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的截图。
- do(action="Tap", element=[x,y], message="重要操作")  
    基本功能同Tap，点击涉及财产、支付、隐私等敏感按钮时触发。
- do(action="Type", text="xxx")  
    Type是输入操作，在当前聚焦的输入框中输入文本。使用此操作前，请确保输入框已被聚焦（先点击它）。输入的文本将像使用键盘输入一样输入。重要提示：手机可能正在使用 ADB 键盘，该键盘不会像普通键盘那样占用屏幕空间。要确认键盘已激活，请查看屏幕底部是否显示 'ADB Keyboard {ON}' 类似的文本，或者检查输入框是否处于激活/高亮状态。不要仅仅依赖视觉上的键盘显示。自动清除文本：当你使用输入操作时，输入框中现有的任何文本（包括占位符文本和实际输入）都会在输入新文本前自动清除。你无需在输入前手动清除文本——直接使用输入操作输入所需文本即可。操作完成后，你将自动收到结果状态的截图。
- do(action="Type_Name", text="xxx")  
    Type_Name是输入人名的操作，基本功能同Type。
- do(action="Interact")  
    Interact是当有多个满足条件的选项时而触发的交互操作，询问用户如何选择。
- do(action="Swipe", start=[x1,y1], end=[x2,y2])  
    Swipe是滑动操作，通过从起始坐标拖动到结束坐标来执行滑动手势。可用于滚动内容、在屏幕之间导航、下拉通知栏以及项目栏或进行基于手势的导航。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。滑动持续时间会自动调整以实现自然的移动。此操作完成后，您将自动收到结果状态的截图。
- do(action="Note", message="True")  
    记录当前页面内容以便后续总结。
- do(action="Call_API", instruction="xxx")  
    总结或评论当前页面或已记录的内容。
- do(action="Long Press", element=[x,y])  
    Long Pres是长按操作，在屏幕上的特定点长按指定时间。可用于触发上下文菜单、选择文本或激活长按交互。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的屏幕截图。
- do(action="Double Tap", element=[x,y])  
    Double Tap在屏幕上的特定点快速连续点按两次。使用此操作可以激活双击交互，如缩放、选择文本或打开项目。坐标系统从左上角 (0,0) 开始到右下角（999,999)结束。此操作完成后，您将自动收到结果状态的截图。
- do(action="Take_over", message="xxx")  
    Take_over是接管操作，表示在登录和验证阶段需要用户协助。
- do(action="Back")  
    导航返回到上一个屏幕或关闭当前对话框。相当于按下 Android 的返回按钮。使用此操作可以从更深的屏幕返回、关闭弹出窗口或退出当前上下文。此操作完成后，您将自动收到结果状态的截图。
- do(action="Home") 
    Home是回到系统桌面的操作，相当于按下 Android 主屏幕按钮。使用此操作可退出当前应用并返回启动器，或从已知状态启动新任务。此操作完成后，您将自动收到结果状态的截图。
- do(action="Wait", duration="x seconds")  
    等待页面加载，x为需要等待多少秒。
- finish(message="xxx")  
    finish是结束任务的操作，表示准确完整完成任务，message是终止信息。 

必须遵循的规则：
1. 在执行任何操作前，先检查当前app是否是目标app，如果不是，先执行 Launch。
2. 如果进入到了无关页面，先执行 Back。如果执行Back后页面没有变化，请点击页面左上角的返回键进行返回，或者右上角的X号关闭。
3. 如果页面未加载出内容，最多连续 Wait 三次，否则执行 Back重新进入。
4. 如果页面显示网络问题，需要重新加载，请点击重新加载。
5. 如果当前页面找不到目标联系人、商品、店铺等信息，可以尝试 Swipe 滑动查找。
6. 遇到价格区间、时间区间等筛选条件，如果没有完全符合的，可以放宽要求。
7. 在做小红书总结类任务时一定要筛选图文笔记。
8. 购物车全选后再点击全选可以把状态设为全不选，在做购物车任务时，如果购物车里已经有商品被选中时，你需要点击全选后再点击取消全选，再去找需要购买或者删除的商品。
9. 在做外卖任务时，如果相应店铺购物车里已经有其他商品你需要先把购物车清空再去购买用户指定的外卖。
10. 在做点外卖任务时，如果用户需要点多个外卖，请尽量在同一店铺进行购买，如果无法找到可以下单，并说明某个商品未找到。
11. 请严格遵循用户意图执行任务，用户的特殊要求可以执行多次搜索，滑动查找。比如（i）用户要求点一杯咖啡，要咸的，你可以直接搜索咸咖啡，或者搜索咖啡后滑动查找咸的咖啡，比如海盐咖啡。（ii）用户要找到XX群，发一条消息，你可以先搜索XX群，找不到结果后，将"群"字去掉，搜索XX重试。（iii）用户要找到宠物友好的餐厅，你可以搜索餐厅，找到筛选，找到设施，选择可带宠物，或者直接搜索可带宠物，必要时可以使用AI搜索。
12. 在选择日期时，如果原滑动方向与预期日期越来越远，请向反方向滑动查找。
13. 执行任务过程中如果有多个可选择的项目栏，请逐个查找每个项目栏，直到完成任务，一定不要在同一项目栏多次查找，从而陷入死循环。
14. 在执行下一步操作前请一定要检查上一步的操作是否生效，如果点击没生效，可能因为app反应较慢，请先稍微等待一下，如果还是不生效请调整一下点击位置重试，如果仍然不生效请跳过这一步继续任务，并在finish message说明点击不生效。
15. 在执行任务中如果遇到滑动不生效的情况，请调整一下起始点位置，增大滑动距离重试，如果还是不生效，有可能是已经滑到底了，请继续向反方向滑动，直到顶部或底部，如果仍然没有符合要求的结果，请跳过这一步继续任务，并在finish message说明但没找到要求的项目。
16. 在做游戏任务时如果在战斗页面如果有自动战斗一定要开启自动战斗，如果多轮历史状态相似要检查自动战斗是否开启。
17. 如果没有合适的搜索结果，可能是因为搜索页面不对，请返回到搜索页面的上一级尝试重新搜索，如果尝试三次返回上一级搜索后仍然没有符合要求的结果，执行 finish(message="原因")。
18. 在结束任务前请一定要仔细检查任务是否完整准确的完成，如果出现错选、漏选、多选的情况，请返回之前的步骤进行纠正。
""".trimIndent()

class RutoGLM(
    private val bigModelSession: BigModelSession,
    private val runtime: RutoRuntime,
    private val device: DeviceRepo,
    var displayId: Int = Display.DEFAULT_DISPLAY
) {
    var quality: Int = 0

    var delayDuration: Duration = 0.5.seconds

    constructor(
        bigModel: BigModel,
        prompt: String,
        runtime: RutoRuntime,
        device: DeviceRepo,
        displayId: Int = Display.DEFAULT_DISPLAY
    ) : this(
        bigModel.openSession(prompt), runtime, device, displayId
    )

    constructor(
        model: StreamingChatModel,
        prompt: String,
        runtime: RutoRuntime,
        device: DeviceRepo,
        displayId: Int = Display.DEFAULT_DISPLAY
    ) : this(BigModel(model), prompt, runtime, device, displayId)

    constructor(
        modelUrl: String,
        modelName: String,
        apiKey: String,
        prompt: String,
        runtime: RutoRuntime,
        device: DeviceRepo,
        displayId: Int = Display.DEFAULT_DISPLAY
    ) : this(
        OpenAiStreamingChatModel.builder()
            .baseUrl(modelUrl)
            .modelName(modelName)
            .apiKey(apiKey)
            .temperature(0.1)
            .maxTokens(3000)
            .topP(0.85)
            .frequencyPenalty(0.2)
            .httpClientBuilder(RetrofitHttpClientBuilderFactory().create())
            .build(), prompt, runtime, device, displayId
    )

    private fun toImageContent(bitmap: Bitmap?): ImageContent? {
        if (bitmap == null) return null

        val bytes = try {
            val stream = ByteArrayOutputStream()
            val format =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) Bitmap.CompressFormat.WEBP_LOSSY
                else Bitmap.CompressFormat.WEBP
            File("/data/user/0/com.rosan.ruto/files/a.webp").apply { createNewFile() }
                .outputStream().use {
                    bitmap.compress(format, quality, it)
                }
            bitmap.compress(format, quality, stream)
            stream.toByteArray()
        } finally {
            bitmap.recycle()
        }

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)

        val mimeType = options.outMimeType
        val base64String = Base64.encodeToString(bytes, Base64.NO_WRAP)

        return ImageContent(base64String, mimeType)
    }

    suspend fun ruto(
        text: String? = null,
        onStreaming: (think: String) -> Unit = {},
        onComplete: (think: String) -> Unit = {},
        onCapture: suspend () -> Bitmap?
    ): String {
        Log.e("r0s", "task $text")
        val bitmap = onCapture()
        Log.e("r0s", "bitmap $bitmap")
        val image = toImageContent(bitmap)
        Log.e("r0s", "capture $image")
        Log.e("r0s", "request $image")
        var aiMessage = ""
        var content = ""
        bigModelSession.request(text, image).onEach { reply ->
            if (reply !is StreamingChatModelReply.PartialResponse) return@onEach
            aiMessage += reply.partialResponse
            val parsed = GLMCommandParser.parse(aiMessage)
            content = when (parsed) {
                GLMCommandParser.Status.None -> aiMessage
                is GLMCommandParser.Status.Triggered -> parsed.prefixText
                is GLMCommandParser.Status.Pending -> parsed.prefixText
                is GLMCommandParser.Status.Completed -> parsed.prefixContent + parsed.suffixContent
            }
            onStreaming.invoke(content)
        }.collect {
            onComplete.invoke(content)
        }

        val parsed = GLMCommandParser.parse(aiMessage)
        Log.e("r0s", "parsed ${parsed}")
        if (parsed !is GLMCommandParser.Status.Completed) return ""
        Log.e("r0s", "func ${parsed.command.mapping} : ${parsed.arguments}")
        runtime.registerFunction("finish") {
            throw RutoFinishException("")
        }
        try {
            val result = parsed.callFunction(runtime)
            delay(1000)
            return ruto(
                if (result == Unit) "" else result.toString(),
                onStreaming,
                onComplete,
                onCapture
            )
        } catch (e: RutoFinishException) {
            return ""
        } finally {
            runtime.unregisterFunction("finish")
        }
    }
}