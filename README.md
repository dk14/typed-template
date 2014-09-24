typed-template
==============

This macro generates parser, based on user-friendly simplified grammar from external resource.
AST returned by parser will be typesafe (tree of case classes)

Example

Grammar:

    instrument {name} is Option
    exercise at {exerciseDate: Date} {automatic: String}
      {optionsCount: Count} options with strike {strike: Money}
      with {count: Count = one} underlyer
      pay premium {premium: Money} from {partyFrom: Party} to {partyTo: Party} at {premiumDate: Date}
  

Input:

    instrument Equty is Option
      exercise at 10500/10/10 automatic
      20 options with strike 100
      with one underlyer
      pay premium 100500 from DB to NEDB at 10500/10/10  


AST: 

    Option(ExerciseDate(10500/10/10,automatic),OptionsCount(20,100),Count(one),Premium(100500,DB,NEDB,10500/10/10))
