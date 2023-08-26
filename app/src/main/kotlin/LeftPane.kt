import files.isDirectory
import javafx.event.EventHandler
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.MenuBar
import javafx.scene.control.SelectionMode
import javafx.scene.control.ToolBar
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.*
import javafx.scene.paint.Color


/*
    Since both the bottom and centre panes depend on the selection of the left pane, they get implicitly
    made when creating a left pane
*/
fun createLeftPane(currentDirectory : String, dirContent : List<String>, root : BorderPane, keyEvent : EventHandler<KeyEvent>, mouseEvent : EventHandler<MouseEvent>, prevItem : String? = null) : ListView<String> {
    val leftPane = ListView<String>().apply {
        for (i in dirContent) {
            items.add(i)
        }
        selectionModel.selectedItemProperty().addListener { _,_, _ ->
            root.bottom = createBottomPane(currentDirectory + selectionModel.selectedItem)
            root.center = createCenterPane(currentDirectory + selectionModel.selectedItem)
        }

        /*
        You might think to yourself: "Wow, what a terrible solution enabling buttons. This is so inefficient for even 6
        items" And you would be right. Nothing in the spec ever says it was required, but in class it was mentioned to be bad
        design if it was missing. I do not want to refactor all my code, get new bugs, and debug them in 1 night. So
        This is what we end up with, it should be a nice bonus anyway, and Jeff said it was fine (bindings for next time)
         */

        selectionModel.selectedItemProperty().addListener { _,_, newValue ->
            // disable Next button/menu
            val isNotDir = !isDirectory(currentDirectory + newValue)
            ((root.top as VBox).children[0] as MenuBar).menus[0].items[1].isDisable = isNotDir // menu
            ((root.top as VBox).children[1] as ToolBar).items[1].isDisable = isNotDir // toolbar
        }

        // disable Home/Previous buttom/menu, these are independent of the selected item, so just check on creation
        val isRootDir = currentDirectory == rootDirectory
        // Home
        ((root.top as VBox).children[0] as MenuBar).menus[0].items[0].isDisable = isRootDir // menu
        ((root.top as VBox).children[1] as ToolBar).items[0].isDisable = isRootDir // toolbar

        // Previous
        ((root.top as VBox).children[0] as MenuBar).menus[0].items[2].isDisable = isRootDir// menu
        ((root.top as VBox).children[1] as ToolBar).items[2].isDisable = isRootDir // toolbar

        addEventFilter(KeyEvent.KEY_PRESSED, keyEvent)
        addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent)

        selectionModel.selectionMode = SelectionMode.SINGLE

        if (prevItem != null) {
            selectionModel.select(items.indexOf(prevItem))
        }
        else {
            selectionModel.select(0);
        }

        // select will not apply here, manually set the center and bottom views
        if (items.isEmpty()) {
            root.center = StackPane().apply {
                prefWidth = 100.0
                background = Background(BackgroundFill(Color.ANTIQUEWHITE, null, null))
                children.add(VBox().apply {
                    children.add(Label("Empty Directory"))
                    alignment = Pos.CENTER
                })
                alignment = Pos.CENTER
            }
            root.bottom = createBottomPane(currentDirectory)
            // disable next options because dir is empty
            ((root.top as VBox).children[0] as MenuBar).menus[0].items[1].isDisable = true // menu
            ((root.top as VBox).children[1] as ToolBar).items[1].isDisable = true // toolbar
        }

        requestFocus()
    }
    return leftPane
}

fun getLeftPaneItem(leftPane : ListView<String>) : String {
    return leftPane.selectionModel.selectedItem
}

fun isLeftPaneEmpty(leftPane: ListView<String>) : Boolean {
    return leftPane.items.isEmpty()
}

fun isDirectorySelected(leftPane: ListView<String>, curDir : String) : Boolean {
    if (leftPane.items.isEmpty()) {
        return false
    }
    else {
        return files.isDirectory(curDir + leftPane.selectionModel.selectedItem)
    }
}