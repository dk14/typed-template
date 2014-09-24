/**
 * Created by user on 9/24/14.
 */



object Test extends App {

  @generator(source = "option.template") object Env

  val instrument = """instrument Equty is Option
    |exercise at 10500/10/10 automatic
    |  20 options with strike 100
    |  with one underlyer
    |  pay premium 100500 from DB to NEDB at 10500/10/10
    |  """.stripMargin

  import Env._
  
  val expected = Option(
    ExerciseDate("10500/10/10","automatic"),
    OptionsCount("20","100"),
    Count("one"),
    Premium("100500","DB","NEDB","10500/10/10")
  )
  
  assert(Env.parse(instrument) == expected)

}
