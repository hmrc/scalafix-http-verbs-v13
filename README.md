# Scalafix rules for http-verbs

## Usage

## Developing:

Please contribute rules!

To develop rule:
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
    
`astexplorer.net`
    
This won't make any changes, but it will print out the code being considered (doc.tree.synatax), but crucially the structure of the AST.

