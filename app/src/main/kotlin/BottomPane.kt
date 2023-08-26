import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.VBox

fun createBottomPane(directory : String) : VBox {
    return VBox(Label(directory)).apply{
        prefHeight = 10.0
        alignment = Pos.CENTER_LEFT
    }
}
