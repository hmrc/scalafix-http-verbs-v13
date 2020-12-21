# Scalafix rules for http-verbs

## Usage

As per [the scalafix guide for external rules](https://scalacenter.github.io/scalafix/docs/rules/external-rules.html), you can invoke the rules via command line.

For example, this runs the OptionalConfig scalafix rule:

    sbt ";scalafixEnable;scalafix dependency:OptionalConfig@uk.gov.hmrc:scalafix-http-verbs-v13:0.1.0-SNAPSHOT"

## Limitations

These scalafix rules will never find and fix 100% of the required changes. We're taking a view that if the scalafix rules cover the basic use-cases, this will account for a large majority of code on the platform.

such as the contentious issue of code formatting after scalafix, can be remedied by running other tools after scalafix. Running linters and formatters before scalafix can also increase the chances of rules being applied correctly too.

We're hoping these limitations are reduced in time through continued use, and feedback from the people applying them.

## Contributing

We would love to accept contributions towards these rules. Please submit PRs for any changes you think would benefit others.

We're still learning and experimenting with this tool, so any constructive feedback is welcomed too.

To develop rules:
```
sbt ~tests/test
# edit rules/src/main/scala/fix/Httpverbs.scala
```

## Learning Scalafix

Read the docs. Then play around!

Every scalafix seems to start with a `doc`. That has a `tree` method on it, and that has three useful methods:
- Traverse (a bit like `foreach`: `Tree => Unit`)
- Transform (a bit like `flatMap` in a way: `Tree => Tree`)
- Collect (a bit like `fold`: `Tree => T`) where T is usually a `Patch` of changes.

Every rule can start off by doing the following though:

    override def fix(implicit doc: SemanticDocument): Patch = { 
        println(doc.tree.syntax)
        println(doc.tree.structure)
        
        Patch.empty
    }
    
This won't make any changes, but will display the code (the syntax) and the AST (the structure). You can view the AST structure by pasting code into [astexplorer.net](https://astexplorer.net/) too.
