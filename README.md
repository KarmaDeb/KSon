# KSON
KarmaDev Json, or KSON to abbreviate, is a java library 
which helps in parsing and reading json objects. It also
allows to serialize objects directory to json and load
them later, all by using annotations

# Keys, paths and path separators
In Kson, every json element is conscious of who he is,
this means that every object knows in which key he is
assigned to, for instance, a number "5" in {"key": 5}
knows that he's assigned under "key". To accomplish that,
you must give the name to every element but the first
or "main" one. You can also specify a element path, which
is the route to follow to get into an element, and a
path separator when navigating.

This is all because in Kson, you can retrieve any child
item of an object directly, without iterating nor doing
multiple `getAsObject()`. That's what the path is for and
the path separator. For instance, on a json file like this:

```json
{
  "co1": {
    "co2": {
      "co3": {
        "key": "value"
      }
    }
  }
}
```

You could just do the following:
```java
JsonInstance instance = JsonReader.read(exampleJson);
String key = instance.asObject()
                .getChild("co1.co2.co3.key").asString();
```

# API Example

### Reading a json string
When reading a json, you can read from a `Reader`, 
`InputStream`, `String` and `byte[]`. In all cases a valid
json is expected, in case of bytes, it would be
the byte representation of a json string

```java
import es.karmadev.api.kson.JsonInstance;
import es.karmadev.api.kson.io.JsonReader;

public final class MyClass {
    
    public static void main(final String[] args) {
        final String exampleJson = "{\"key\": \"value\"}";
        JsonInstance instance = JsonReader.read(exampleJson);

        boolean doPretty = true;
        System.out.println(instance.toString(doPretty));
    }
}
```

### Writing a json
Writing a json is very easy, once you know the schema
your json will follow.

# IMPORTANT!
**NOT SPECIFYING A KEY WILL CAUSE THE JSON TO HAVE EMPTY
KEYS**

```
JsonObject#newObject(String:key)
JsonObject#newObject(String:path, String: key) 
JsonObject#newObject(String:key, char:path separator)
JsonObject#newObject(String:path, String:key, char:path separator)
```

```java
import es.karmadev.api.kson.JsonObject;

public final class MyClass {

    public static void main(final String[] args) {
        JsonObject object = JsonObject.newObject("");
        JsonObject ob1 = JsonObject.newObject("co1");
        JsonObject ob2 = JsonObject.newObject("co1", "co2");
        JsonObject ob3 = JsonObject.newObject("co1.co2", "co3");
        
        object.put("co1", ob1);
        ob1.put("co2", ob2);
        ob2.put("co3", ob3);
        
        ob3.put("key", "value");
        String raw = object.toString();
    }
}
```

# Serializing
Kson allows to serialize a class very easily, in a
json format and load it later, without having to implement
anything _except for transformers for special fields_.

```java
import es.karmadev.api.kson.processor.JsonCollection;
import es.karmadev.api.kson.processor.JsonSerializable;
import es.karmadev.api.kson.processor.construct.CollectionParam;
import es.karmadev.api.kson.processor.construct.JsonConstructor;
import es.karmadev.api.kson.processor.construct.JsonParameter;
import es.karmadev.api.kson.processor.conversor.UUIDTransformer;
import es.karmadev.api.kson.processor.field.JsonElement;
import es.karmadev.api.kson.processor.field.JsonExclude;
import es.karmadev.api.kson.processor.field.Transformer;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@JsonSerializable
public class Customer {

    @JsonElement
    private final long id;

    @JsonElement(name = "name")
    private final String customer;

    @JsonElement
    private final Wallet wallet;

    @JsonElement
    @JsonCollection
    private final List<Transaction> transactions;

    @JsonElement
    @Transformer(transformer = UUIDTransformer.class)
    private final UUID customerId;

    public Customer(final long id, final String name) {
        this.id = id;
        this.customer = name;
        this.wallet = new Wallet(id, id, 500d);
        this.transactions = new ArrayList<>();
        this.customerId = UUID.randomUUID();
    }

    @JsonConstructor
    private Customer(final @JsonParameter(readFrom = "id") long id,
                     final @JsonParameter(readFrom = "name") String customer,
                     final @JsonParameter(readFrom = "wallet") Wallet wallet,
                     final @JsonParameter(readFrom = "transactions")
                     @CollectionParam(collectionType = Transaction.class)
                     List<Transaction> transactions,
                     final @JsonParameter(readFrom = "customerId") UUID customerId) {
        this.id = id;
        this.customer = customer;
        this.wallet = wallet;
        this.transactions = transactions;
        this.customerId = customerId;
    }
    
    public void addTransaction(final Transaction transaction) {
        this.transactions.add(transaction);
    }
}
```

```java
@JsonSerializable(allPublic = true)
public class Wallet {

    public final long id;
    public final long customerId;
    public final double currency;

    @JsonSerializable
    public Wallet(final @JsonParameter(readFrom = "id") long id,
                  final @JsonParameter(readFrom = "customerId") long customerId,
                  final @JsonParameter(readFrom = "currency") double currency) {

    }
}

@JsonSerializable(allPublic = true)
public class Transaction {

    public final long id;
    public final long customerId;
    @JsonExclude
    public transient long appTempSerial;

    @JsonSerializable
    private final double cost;
    
    public Transaction(final @JsonParameter(readFrom = "id") long id,
                       final @JsonParameter(readFrom = "customerId") long customerId,
                       final @JsonParameter(readFrom = "cost") double cost) {
        this.id = id;
        this.customerId = customerId;
        this.cost = cost;

        this.appTempSerial = ThreadLocalRandom.current().nextLong();
    }
}
```

```java
import es.karmadev.api.kson.io.JsonWriter;

import java.io.StringWriter;

public class Main {

    public static void main(String[] args) {
        Customer customer = new Customer(1, "John Doe");
        Transaction transaction = new Transaction(1, 1, 25);
        customer.addTransaction(transaction);

        JsonWriter writer = new JsonWriter(customer);
        StringWriter sw = new StringWriter();
        writer.export(sw);
        
        System.out.println(sw);
    }
}

```

This would print the following:

```json
{
  "--serialized": "com.mypackage.Customer",
  "id": 1,
  "name": "John Doe",
  "wallet": {
    "--serialized": "com.mypackage.Wallet",
    "id": 1,
    "customerId": 1,
    "currency": 500
  },
  "transactions": [
    {
      "--serialized": "com.mypackage.Transaction",
      "id": 1,
      "customerId": 1,
      "cost": 25
    }
  ],
  "customerId--transform": "es.karmadev.api.kson.processor.conversor.UUIDTransformer",
  "customerId": "<a random UUID>"
}
```

As you can see, the json would have metadata which
is completely capable of transforming the json object
back into a Customer instance, with its wallet and
transactions

```java
import es.karmadev.api.kson.io.JsonReader;
import es.karmadev.api.kson.io.JsonWriter;

import java.io.StringWriter;

public class Main {

    public static void main(String[] args) {
        Customer customer = new Customer(1, "John Doe");
        Transaction transaction = new Transaction(1, 1, 25);
        customer.addTransaction(transaction);

        JsonWriter writer = new JsonWriter(customer);
        StringWriter sw = new StringWriter();
        writer.export(sw);

        Customer cm = JsonReader.load(Customer.class, sw);
    }
}
```