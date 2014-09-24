typed-template
==============

This macro generates parser, based on user-friendly simplified external grammar.
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
    
Run test: 
    git checkout https://github.com/dk14/typed-template.git
    brew install sbt
    sbt
    ;clean;test/runMain Test
    
Note: macroparadise must be added to use this macro

    autoCompilerPlugins := true,
    resolvers in ThisBuild  += Resolver.sonatypeRepo("releases"),
    libraryDependencies += "org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full)
    

