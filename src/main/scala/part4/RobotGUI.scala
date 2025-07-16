package part4

import it.unibo.u12lab.code.Scala2P.{*, given}
import alice.tuprolog.{SolveInfo, Term}

import java.awt.{BasicStroke, Color, Graphics2D}
import scala.swing.*
import scala.swing.event.*
import scala.swing.Dialog.Message
import javax.swing.{JSpinner, SpinnerNumberModel}
import java.awt.geom.Ellipse2D

type Command = String
type Plan = LazyList[Command]
case class Pos(x: Int, y: Int)

object RobotGUI extends SimpleSwingApplication:

  private val INITIAL_MAX_MOVES = 6
  //Grid
  private val GRID_SIZE: Int = 4
  private val CELL_SIZE: Int = 80
  private val GRID_PIXEL_WIDTH: Int = GRID_SIZE * CELL_SIZE
  private val GRID_PIXEL_HEIGHT: Int = GRID_SIZE * CELL_SIZE
  private var mainContentPanel: BorderPanel = _

  //GUI constants
  private val OFFSET: Int = 200
  private val WINDOW_WIDTH: Int = GRID_PIXEL_WIDTH + OFFSET
  private val WINDOW_HEIGHT: Int = GRID_PIXEL_HEIGHT + OFFSET

  //Robot
  private var robotPosition: Pos = Pos(0, 0)
  private var currentPlan: Plan = LazyList()
  private var plans: LazyList[Plan] = LazyList()
  private var currentStep: Int = 0
  var isExecuting: Boolean = false

  //Prolog
  private val prologTheory: String = loadPrologTheory("src/main/prolog/Robot.pl")
  val engine: Term => LazyList[SolveInfo] = mkPrologEngine(prologTheory)

  override def top: Frame = new MainFrame:
    title = "Robot"
    preferredSize = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
    minimumSize = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
    centerOnScreen()
    resizable = true

    //Header Panel
    val northPanel: BorderPanel = new BorderPanel:
      preferredSize = new Dimension(0, 50)
      minimumSize = new Dimension(0, 50)
      val headerLabel: Label = new Label("Robot Planner - Lab 12"):
        font = new Font("Arial", java.awt.Font.BOLD, 18)
        horizontalAlignment = Alignment.Center
      layout(headerLabel) = BorderPanel.Position.Center

    //Grid Panel
    val gridPanel: Panel = new Panel:
      focusable = true
      override def paintComponent(g: Graphics2D): Unit = {
        super.paintComponent(g)
        drawGrid(g, size.width, size.height)
        drawRobot(g, size.width, size.height)
      }

    //Control Panel
    val controlPanelInput: BoxPanel = new BoxPanel(Orientation.Horizontal):
      preferredSize = new Dimension(0, 50)
      minimumSize = new Dimension(0, 50)
      val labelInputMaxMoves: Label = Label(s"Insert max moves: ")
      val generateButton: Button = new Button(s"Generate solutions")
      val inputMaxMoves = SpinnerNumberModel(INITIAL_MAX_MOVES, 0, 100, 1)
      val componentInputMaxMoves: Component = new Component:
        preferredSize = new Dimension(50, 25)
        maximumSize = preferredSize
        override lazy val peer = JSpinner(inputMaxMoves)
      contents ++= List(Swing.HGlue, labelInputMaxMoves, Swing.HStrut(10), componentInputMaxMoves, Swing.HStrut(10), generateButton, Swing.HGlue)
      listenTo(generateButton)
        reactions += {
          case ButtonClicked(`generateButton`) => generatePlans(inputMaxMoves.getValue.asInstanceOf[Int])
        }

    val controlPanelStep: BoxPanel = new BoxPanel(Orientation.Horizontal):
      preferredSize = new Dimension(0, 50)
      minimumSize = new Dimension(0, 50)
      val stepButton: Button = new Button(s"Step")
      listenTo(stepButton)
        reactions += {
          case ButtonClicked(`stepButton`) => doStep()
        }
      contents ++= List(Swing.HGlue, stepButton, Swing.HGlue)

    val controlPanelCompletedPlan: BoxPanel = new BoxPanel(Orientation.Horizontal):
      preferredSize = new Dimension(0, 50)
      minimumSize = new Dimension(0, 50)
      val nextPlanButton: Button = new Button(s"Step")
      val changeMaxMovesButton: Button = new Button(s"Insert new \"max moves\"")
      listenTo(nextPlanButton, changeMaxMovesButton)
      reactions += {
        case ButtonClicked(`nextPlanButton`) => doStep()
      }
      contents ++= List(Swing.HGlue, nextPlanButton, Swing.HStrut(10), changeMaxMovesButton, Swing.HGlue)

    //Util functions (drawing)
    def drawGrid(g: Graphics2D, panelWidth: Int, panelHeight: Int): Unit =
      g setColor Color.BLACK
      g setStroke new BasicStroke(2)
      val offsetX = (panelWidth - GRID_PIXEL_WIDTH) / 2
      val offsetY = (panelHeight - GRID_PIXEL_HEIGHT) / 2
      0 to GRID_SIZE foreach { i =>
        val yPos = i * CELL_SIZE + offsetY
        g drawLine (offsetX, yPos, GRID_PIXEL_WIDTH + offsetX, yPos)
        val xPos = i * CELL_SIZE + offsetX
        g drawLine (xPos, offsetY, xPos, GRID_PIXEL_HEIGHT + offsetY)
      }

    def drawRobot(g: Graphics2D, panelWidth: Int, panelHeight: Int): Unit =
      g setColor Color.ORANGE
      val offsetX = (panelWidth - GRID_PIXEL_WIDTH) / 2
      val offsetY = (panelHeight - GRID_PIXEL_HEIGHT) / 2
      val robotDiameter = CELL_SIZE - 30
      val robotCellOffsetX = (CELL_SIZE - robotDiameter) / 2
      val robotCellOffsetY = (CELL_SIZE - robotDiameter) / 2
      val robotDrawX = robotPosition.x * CELL_SIZE + offsetX + robotCellOffsetX
      val robotDrawY = robotPosition.y * CELL_SIZE + offsetY + robotCellOffsetY
      val robotCircle = new Ellipse2D.Double(robotDrawX, robotDrawY, robotDiameter, robotDiameter)
      g fill robotCircle
      g setColor Color.BLACK
      g setStroke new BasicStroke(2)
      g draw robotCircle

    //Util functions (prolog)
    extension (l: LazyList[SolveInfo])
      def getOutputPositions: LazyList[Pos] = l map :
        s => Pos(extractTerm(s, "X").toString.toInt, extractTerm(s, "Y").toString.toInt)
      def getFirstOutputPos: Pos = l.getOutputPositions.head

    def goalPosition: Pos = engine("goal(s(X,Y))").getFirstOutputPos

    def generatePlans(maxMoves: Int): Unit =
      val results: LazyList[SolveInfo] = engine(s"plan($maxMoves, Plan)")
      if results.isEmpty then
        Dialog.showMessage(title = s"No plan found", message = s"Could not find a solution in $maxMoves steps", messageType = Message.Info)
      else
        plans = results extractSolutionsOf "Plan" map {extractListFromTerm(_)}
        currentPlan = plans.head
        plans = plans.tail
        mainContentPanel.layout(controlPanelStep) = BorderPanel.Position.South
        mainContentPanel.revalidate()
        mainContentPanel.repaint()


    def doStep(): Unit =
      if currentStep < currentPlan.length then
        val command: Command = currentPlan(currentStep)
        robotPosition = engine(s"move($command, s(${robotPosition.x}, ${robotPosition.y}), s(X, Y))").getFirstOutputPos
        currentStep += 1
        mainContentPanel.revalidate()
        mainContentPanel.repaint()
        println(s"$robotPosition")
        if currentStep >= currentPlan.length then
          Dialog.showMessage(title = s"Plan completed", message = s"You reached your goal position.", messageType = Message.Info)

    //Drawing
    mainContentPanel = new BorderPanel:
      import BorderPanel.Position
      layout(northPanel) = Position.North
      layout(gridPanel) = Position.Center
      layout(controlPanelInput) = Position.South
    contents = mainContentPanel