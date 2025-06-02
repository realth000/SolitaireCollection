package kzs.th000.solitaire_collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CardMinimalExample() {
    Card(
        modifier = Modifier.width(400.dp).height(400.dp).padding(12.dp).border(width = 2.dp, color = Color.Blue)
    ) {
        Column {
            DraggableText("world 1")
            DraggableText("world 2")
            Box(modifier = Modifier.size(200.dp))
            DropBox()
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
fun DraggableText(text: String) {
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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
@Composable
fun DropBox() {
    var showTargetBorder by remember { mutableStateOf(false) }
    var targetText by remember { mutableStateOf("DROP HERE") }
    val coroutineScope = rememberCoroutineScope()
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
                val result = (targetText == "Drop here")
                targetText = event.awtTransferable.let {
                    if (it.isDataFlavorSupported(DataFlavor.stringFlavor))
                        it.getTransferData(DataFlavor.stringFlavor) as String
                    else
                        it.transferDataFlavors.first().humanPresentableName
                }
                // Set text to the initial one after 2 seconds.
                coroutineScope.launch {
                    delay(2000)
                    targetText = "Drop here"
                }
                return result
            }
        }
    }

    Box(
        Modifier.size(200.dp).background(Color.Red).then(
            if (showTargetBorder)
                Modifier.border(BorderStroke(3.dp, Color.Black))
            else Modifier
        ).dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dragAndDropTarget)
    ) {
        Text(targetText, Modifier.align(Alignment.Center))
    }
}