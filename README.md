typed-template
==============

This macro generates parser, based on user-friendly simplified grammar.
AST returned by parser will be typesafe (tree of case classes)

Example

Grammar:

    instrument {name} is Option
    exercise at {exerciseDate: String} {automatic: String}
      {optionsCount: String} options with strike {strike: String}
      with {count: String} underlyer
      pay premium {premium: String} from {partyFrom: String} to {partyTo: String} at {premiumDate: String}
  

Input:

    instrument Equty is Option
      exercise at 10500/10/10 automatic
      20 options with strike 100
      with one underlyer
      pay premium 100500 from DB to NEDB at 10500/10/10  


Code:
   
    @generator(source = "option.template") object Env
    Env.parse(instrument)


Output AST: 

    Env.Option(
      Env.ExerciseDate(10500/10/10,automatic),
      Env.OptionsCount(20,100),
      Env.Count(one),
      Env.Premium(100500,DB,NEDB,10500/10/10)
    )
    

