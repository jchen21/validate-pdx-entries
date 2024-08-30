This function is executed on the servers that host the partitioned region. Please run it on a test environment first.  
It iterates on entries of the region. 
If the PDX `read-serialized` property is `false`, it deserializes the entry value. 
Otherwise, If the PDX `read-serialized` property is `true` and the entry value is an instance of `PdxInstanceImpl`, 
it calls `PdxInstance.getField()` on all `PdxField`s of the `PdxInstance`. 
If there is any exception when deserializing the entry value, it will be logged on the servers on which 
the function is running.
If there is any exception when calling `PdxInstance.getField()`, it will also be logged. Only the first `PdxField`
that has the exception will be logged. The rest of the `PdxField`s of the `PdxInstance` in the entry will be skipped.
Please consider the performance impact when running this function, since it is iterating all the region entries and 
all the `PdxField`s.

* Compile the code:

e.g. 
```shell
javac -cp geode-dependencies.jar com/broadcom/gemfire/example/ValidateAllEntriesFunction.java
```

* Package the function to a jar file

e.g.
```shell
jar cf validate.jar com
```

* Deploy the jar using gfsh

e.g.
```shell
deploy --jar=/path/to/validate.jar
```

* Execute the function using gfsh with `--region` option to validate all entries

e.g.
```shell
execute function --id=ValidateAllEntriesFunction --region=testRegion
```

