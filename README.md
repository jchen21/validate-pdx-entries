DO NOT RUN THE FUNCTION(ValidateAllEntriesFunction) IN PRODUCTION

This function(ValidateAllEntriesFunction) is executed on the servers that host the partitioned region.  
It iterates on all the entries of the region. 
If the PDX `read-serialized` property is `false`, it deserializes the entry value. 
Otherwise, If the PDX `read-serialized` property is `true` and the entry value is an instance of `PdxInstanceImpl`, 
it calls `PdxInstance.getField()` on all `PdxField`s of the `PdxInstance`. 
If there is any exception when deserializing the entry value, it will be logged on the servers on which 
the function is running.
If there is any exception when calling `PdxInstance.getField()`, it will also be logged. Only the first `PdxField`
that has an exception will be logged. The rest of the `PdxField`s of the `PdxInstance` in the entry will be skipped.

If an entry value is a `Collection` or a `Map`, and a `PdxInstance` is in the `Collection` or the `Map`, 
or nested in a wrapper, please modify the `validateEntry` method accordingly. 
So that the enclosed `PdxInstance` can be validated.

Please consider the performance impact when running this function, since it iterates on all the region entries and 
all the `PdxField`s.

* Compile the code:

e.g. 
```shell
javac -cp geode-dependencies.jar com/broadcom/gemfire/example/ValidateAllEntriesFunction.java
```

* Pack the function in a jar

e.g.
```shell
jar cf validate.jar com
```

* Deploy the jar using `gfsh`

e.g.
```shell
deploy --jar=/path/to/validate.jar
```

* Execute the function using `gfsh` with `--region` option to validate all entries

e.g.
```shell
execute function --id=ValidateAllEntriesFunction --region=testRegion
```

