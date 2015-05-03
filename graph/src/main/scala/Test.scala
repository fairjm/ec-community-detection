import com.cc.graph.base.Graph

object Test extends App {
  val graph = Graph.load("src/main/resources/Dolphin.txt")
  println(graph)
  graph.display
}