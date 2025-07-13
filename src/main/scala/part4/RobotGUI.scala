package part4

import it.unibo.u12lab.code.Scala2P.{*, given}
import alice.tuprolog.{SolveInfo, Term}

import scala.swing.*
import scala.swing.event.*
import javax.swing.{JSpinner, SpinnerNumberModel, Timer}

object RobotGUI extends SimpleSwingApplication:

  val WINDOW_WIDTH: Int = 400
  val WINDOW_HEIGHT: Int = 400

  override def top: Frame = new MainFrame:
    title = "Robot"
    preferredSize = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
