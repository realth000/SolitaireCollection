package kzs.th000.solitaire_collection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import co.touchlab.kermit.Logger
import com.composables.icons.lucide.Club
import com.composables.icons.lucide.Diamond
import com.composables.icons.lucide.Heart
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Spade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

data class PokerListId(
    val listId: String,
)

data class PokerCardUiState(
    val pokerMap: Map<PokerListId, List<Poker>> = mapOf(),
)

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

data class Poker(val suit: PokerSuit, val rank: PokerRank) {
    override fun toString(): String {
        return "${suit.name}${rank.rank}"
    }

    fun id(listId: PokerListId): String {
        return "$listId${suit.name}${rank.rank}"
    }
}

class PokerListViewModel(val pokers: MutableMap<PokerListId, List<Poker>>) : ViewModel() {
    private val _uiState = MutableStateFlow(PokerCardUiState(pokers));
    val uiState = _uiState.asStateFlow()

    fun cardMoveOut(listId: PokerListId, poker: Poker) {
        val targetPokerList = _uiState.value.pokerMap[listId] ?: return
        if (!targetPokerList.contains(poker)) {
            Logger.e("intended to move poker $poker out of card which not present")
            return
        }

        _uiState.update { state ->
            state.copy(pokerMap = state.pokerMap - listId + Pair(listId, targetPokerList - poker))
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun PokerList(listId: PokerListId, state: PokerCardUiState, viewModel: PokerListViewModel) {

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

    Column(
        verticalArrangement = Arrangement.spacedBy((-20).dp)
    ) {
        Logger.i("UPDATE! pokers=${state.pokerMap}")
        // The state MUST hold current card list.
        state.pokerMap.filter { it -> it.key == listId }.entries.firstOrNull()!!.value
            .map { it ->
                key(it.id(listId)) {
                    PokerCard(listId, it, viewModel)
                }
            }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun PokerCard(listId: PokerListId, poker: Poker, viewModel: PokerListViewModel) {
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
            .border(BorderStroke(2.dp, color = poker.suit.color))
            .drawWithContent {
                graphicsLayer.record {
                    this@drawWithContent.drawContent()
                }
                this@drawWithContent.drawContent()
            }
            .dragAndDropSource(
                drawDragDecoration = {
                    // The decoration when dragging is the contents of current composable.
                    imageBitmap?.let {
                        drawImage(it)
                    }
                }
            ) {
                detectDragGestures(
                    onDragEnd = {
                        viewModel.cardMoveOut(listId, poker)
                    },
                    onDragStart = { offset ->
                        startTransfer(
                            DragAndDropTransferData(
                                transferable = DragAndDropTransferable(
                                    StringSelection(poker.toString())
                                ),
                                supportedActions = listOf(
                                    DragAndDropTransferAction.Copy,
                                    DragAndDropTransferAction.Move,
                                    DragAndDropTransferAction.Link,
                                ),
                                dragDecorationOffset = offset,
                                onTransferCompleted = { action ->
                                    Logger.i("Action at the source: $action, $poker")
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
                        Logger.i("save drag decoration: $poker")
                        imageBitmap = graphicsLayer.toImageBitmap()
                    }
                }
            }
    ) {
        Row {
            Icon(poker.suit.icon, contentDescription = "", tint = poker.suit.color)
            Spacer(modifier = Modifier.size(8.dp, 8.dp))
            Text(poker.rank.rank)
        }
    }
}

@Composable
fun CardMinimalExample() {
    val viewModel = PokerListViewModel(
        pokers = mutableMapOf<PokerListId, List<Poker>>(
            Pair(
                PokerListId("list1"), listOf(
                    Poker(PokerSuit.Diamonds, PokerRank.Rank1),
                    Poker(PokerSuit.Hearts, PokerRank.Rank2),
                    Poker(PokerSuit.Clubs, PokerRank.Rank3),
                    Poker(PokerSuit.Spades, PokerRank.Rank4),
                )
            ),
            Pair(
                PokerListId("list2"), listOf(
                    Poker(PokerSuit.Diamonds, PokerRank.Rank2),
                    Poker(PokerSuit.Hearts, PokerRank.Rank3),
                    Poker(PokerSuit.Clubs, PokerRank.Rank4),
                    Poker(PokerSuit.Spades, PokerRank.Rank5),
                )
            )
        )
    )

    val uiState by viewModel.uiState.collectAsState()

    Card(
        modifier = Modifier.fillMaxSize().padding(12.dp).border(width = 2.dp, color = Color.Blue)
    ) {
        Column(modifier = Modifier.padding(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PokerList(
                    PokerListId("list1"),
                    uiState,
                    viewModel
                )
                PokerList(
                    PokerListId("list2"),
                    uiState,
                    viewModel
                )
            }
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

// /**
//  * Store a list of cards.
//  */
// @OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
// @Composable
// fun PokerBox() {
//     /**
//      * Flag indicating user is dragging some target or not.
//      */
//     var showTargetBorder by remember { mutableStateOf(false) }
//     // val coroutineScope = rememberCoroutineScope()
//     val cardsInside = remember { mutableListOf<String>() }
//
//     val dragAndDropTarget = remember {
//         object : DragAndDropTarget {
//             override fun onStarted(event: DragAndDropEvent) {
//                 showTargetBorder = true
//                 super.onStarted(event)
//             }
//
//             override fun onEnded(event: DragAndDropEvent) {
//                 showTargetBorder = false
//                 super.onEnded(event)
//             }
//
//             override fun onDrop(event: DragAndDropEvent): Boolean {
//                 val incomingCardName = event.awtTransferable.let {
//                     if (it.isDataFlavorSupported(DataFlavor.stringFlavor))
//                         it.getTransferData(DataFlavor.stringFlavor) as String
//                     else
//                         it.transferDataFlavors.first().humanPresentableName
//                 }
//                 cardsInside.add(incomingCardName)
//
//                 // Set text to the initial one after 2 seconds.
//                 // coroutineScope.launch {
//                 //     delay(2000)
//                 //     targetText = "Drop here"
//                 // }
//
//                 // Drop event was consumed or not.
//                 return true
//             }
//         }
//     }
//
//
//     Box(
//         Modifier.size(200.dp).background(Color.Gray).then(
//             if (showTargetBorder)
//                 Modifier.border(BorderStroke(3.dp, Color.Black))
//             else Modifier
//         ).dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dragAndDropTarget)
//     ) {
//         Column {
//             cardsInside.map { it -> Text(it) }
//         }
//     }
// }