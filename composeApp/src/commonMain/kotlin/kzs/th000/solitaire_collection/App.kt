package kzs.th000.solitaire_collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Club
import com.composables.icons.lucide.Diamond
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Spade
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

enum class PokerSuit(val icon: ImageVector, val color: Color) {
    Diamonds(Lucide.Diamond, Color.Red),
    Hearts(Lucide.Heart, Color.Red),
    Clubs(Lucide.Club, Color.Black),
    Spades(Lucide.Spade, Color.Black),
}

enum class PokerRank(val rank: String) {
    Rank1("1"),
    Rank2("2"),
    Rank3("3"),
    Rank4("4"),
    Rank5("5"),
    Rank6("6"),
    Rank7("7"),
    Rank8("8"),
    Rank9("9"),
    Rank10("10"),
    RankJ("J"),
    RankQ("Q"),
    RankK("K"),
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PokerCard(suit: PokerSuit, rank: PokerRank) {
    val graphicsLayer = rememberGraphicsLayer()
    val coroutineScope = rememberCoroutineScope()

    /**
     * Draws contents of current composable widget.
     */
    var imageBitmap by remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    Card(
        modifier = Modifier
            .size(48.dp, 60.dp)
            .border(BorderStroke(2.dp, color = suit.color))
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                this@drawWithContent.drawContent()
            }
            .dragAndDropSource(
                drawDragDecoration = {
                    println(">>> applying drag decoration")
                    // The decoration when dragging is the contents of current composable.
                    imageBitmap?.let {
                        println(">>> draw drag decoration")
                        drawImage(it)
                    }
                }
            ) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startTransfer(
                            DragAndDropTransferData(
                                transferable = DragAndDropTransferable(
                                    StringSelection("${suit.name}:${rank.rank}")
                                ),
                                supportedActions = listOf(
                                    DragAndDropTransferAction.Copy,
                                    DragAndDropTransferAction.Move,
                                    DragAndDropTransferAction.Link,
                                ),
                                dragDecorationOffset = offset,
                                onTransferCompleted = { action ->
                                    println("Action at the source: $action, suit=$suit, rank=$rank")
                                }
                            )
                        )
                    },
                    onDrag = { _, _ -> }
                )
            }.also {
                // This block is called after composable is constructed, save the drawing result before dragging.
                if (imageBitmap == null) {
                    coroutineScope.launch {
                        println(">>> save drag decoration: ${suit.name}${rank.rank}")
                        imageBitmap = graphicsLayer.toImageBitmap()
                    }
                }
            }
    ) {
        Row {
            Icon(suit.icon, contentDescription = "", tint = suit.color)
            Spacer(modifier = Modifier.size(8.dp, 8.dp))
            Text(rank.rank)
        }
    }
}

@Composable
fun CardMinimalExample() {

    Card(
        modifier = Modifier.fillMaxSize().padding(12.dp).border(width = 2.dp, color = Color.Blue)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row {
                PokerCard(PokerSuit.Diamonds, PokerRank.Rank1)
                PokerCard(PokerSuit.Hearts, PokerRank.Rank2)
                PokerCard(PokerSuit.Clubs, PokerRank.Rank3)
                PokerCard(PokerSuit.Spades, PokerRank.Rank4)
            }
            PokerCard("world 1")
            PokerCard("world 2")
            Box(modifier = Modifier.size(200.dp))
            PokerBox()
        }
    }
}

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showContent by remember { mutableStateOf(false) }
        var buttonTip by remember { mutableStateOf("click me") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("hello compose")
                    },
                    navigationIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Favorite,
                            contentDescription = "Nav",
                            tint = Color.Red,
                            modifier = Modifier.size(30.dp),
                        )
                    }

                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier.padding(paddingValues)
            ) {
                Button(onClick = {
                    buttonTip = Greeting.time()
                    showContent = !showContent
                }) {
                    Text(buttonTip)
                }
                CardMinimalExample()
            }
        }

    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PokerCard(text: String) {
    val textMeasurer = rememberTextMeasurer()

    Text(
        modifier = Modifier
            .dragAndDropSource(
                drawDragDecoration = {
                    drawRect(
                        color = Color.White,
                        topLeft = Offset(x = 0f, y = size.height / 4),
                        size = Size(size.width, size.height / 2)
                    )
                    val textLayoutResult = textMeasurer.measure(
                        text = AnnotatedString(text),
                        layoutDirection = layoutDirection,
                        density = this
                    )
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            x = (size.width - textLayoutResult.size.width) / 2,
                            y = (size.height - textLayoutResult.size.height) / 2,
                        )
                    )
                }
            ) {
                detectDragGestures(
                    onDragStart = { offset ->
                        startTransfer(
                            DragAndDropTransferData(
                                transferable = DragAndDropTransferable(
                                    StringSelection(text)
                                ),
                                supportedActions = listOf(
                                    DragAndDropTransferAction.Copy,
                                    DragAndDropTransferAction.Move,
                                    DragAndDropTransferAction.Link,
                                ),
                                dragDecorationOffset = offset,
                                onTransferCompleted = { action ->
                                    println("Action at the source: $action, text=$text")
                                }
                            )
                        )
                    },
                    onDrag = { _, _ -> }
                )
            },
        text = text
    )
}

/**
 * Store a list of cards.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun PokerBox() {
    /**
     * Flag indicating user is dragging some target or not.
     */
    var showTargetBorder by remember { mutableStateOf(false) }
    // val coroutineScope = rememberCoroutineScope()
    val cardsInside = remember { mutableListOf<String>() }

    val dragAndDropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                showTargetBorder = true
                super.onStarted(event)
            }

            override fun onEnded(event: DragAndDropEvent) {
                showTargetBorder = false
                super.onEnded(event)
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                println("Action at the target: ${event.action}")
                val incomingCardName = event.awtTransferable.let {
                    if (it.isDataFlavorSupported(DataFlavor.stringFlavor))
                        it.getTransferData(DataFlavor.stringFlavor) as String
                    else
                        it.transferDataFlavors.first().humanPresentableName
                }
                cardsInside.add(incomingCardName)

                // Set text to the initial one after 2 seconds.
                // coroutineScope.launch {
                //     delay(2000)
                //     targetText = "Drop here"
                // }

                // Drop event was consumed or not.
                return true
            }
        }
    }


    Box(
        Modifier.size(200.dp).background(Color.Gray).then(
            if (showTargetBorder)
                Modifier.border(BorderStroke(3.dp, Color.Black))
            else Modifier
        ).dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dragAndDropTarget)
    ) {
        Column {
            cardsInside.map { it -> Text(it) }
        }
    }
}