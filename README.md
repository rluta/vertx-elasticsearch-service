# Vert.x ElasticSearch Service

Vert.x 3 elasticsearch service with event bus proxying and RxJava support. 
Forked from [ef-labs/vertx-elasticsearch-service](https://github.com/ef-labs/vertx-elasticsearch-service). 

### Version Matrix

| vert.x    | elasticsearch  | vertx-elasticsearch-service     |
| --------- | -------------- | ---------------------------     |
| 3.3.3     | 2.2.2          | 1.0.0                           |


## Configuration

The configuration options are as follows:

```json
{
    "address": <address>,
    "transportAddresses": [ { "hostname": <hostname>, "port": <port> } ],
    "cluster_name": <cluster_name>,
    "client_transport_sniff": <client_transport_sniff>,
    "requireUnits": false
}
```

* `address` - The event bus address to listen on.  (Required)
* `transportAddresses` - An array of transport address objects containing `hostname` and `port`.  If no transport address are provided the default is `"localhost"` and `9300`
    * `hostname` - the ip or hostname of the node to connect to.
    * `port` - the port of the node to connect to.  The default is `9300`.
* `cluster_name` - the elastic search cluster name.  The default is `"elasticsearch"`.
* `client_transport_sniff` - the client will sniff the rest of the cluster and add those into its list of machines to use.  The default is `true`.
* `requireUnits` - boolean flag whether units are required.  The default is `false`.

An example configuration would be:

```json
{
    "address": "eb.elasticsearch",
    "transportAddresses": [ { "hostname": "host1", "port": 9300 }, { "hostname": "host2", "port": 9301 } ],
    "cluster_name": "my_cluster",
    "client_transport_sniff": true
}
```

NOTE: No configuration is needed if running elastic search locally with the default cluster name.


#### Dependency Injection

The `DefaultElasticSearchService` requires a `TransportClientFactory` and `ElasticSearchConfigurator` to be injected.

Default bindings are provided for HK2 and Guice, but you can create your own bindings for your container of choice.

See the [englishtown/vertx-hk2](https://github.com/englishtown/vertx-hk2) or [englishtown/vertx-guice](https://github.com/englishtown/vertx-guice) projects for further details.


## Action Commands

### Index

http://www.elasticsearch.org/guide/reference/api/index_/

An example would be:

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    elasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), jsonResult -> {
        // Do something
    });
    
    final IndexOptions indexOptions = new IndexOptions()
        .setId("123")
        .setOpType(IndexRequest.OpType.INDEX)
        .setTtl(100000l);
        // etc.
    
    elasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexOptions, jsonResult -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    rxElasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"))
        .subscribe(jsonResult -> {
            // Do something
        });
        
    final IndexOptions indexOptions = new IndexOptions()
        .setId("123")
        .setOpType(IndexRequest.OpType.INDEX)
        .setTtl(100000l);
        // etc.
        
    rxElasticSearchService.index("twitter", "tweet", new JsonObject().put("user", "hubrick").put("message", "love elastic search!"), indexOptions)
        .subscribe(jsonResult -> {
            // Do something
        });
```

The event bus replies with a json message with the following structure:

```json
{
    "status": <status>,
    "_index": <index>,
    "_type": <type>,
    "_id": <id>,
    "_version" <version>
}
```

* `status` - either `ok` or `error`
* `index` - the index where the source document is stored
* `type` - the type of source document
* `id` - the string id of the indexed source document
* `version` - the numeric version of the source document starting at 1.

An example reply message would be:

```json
{
    "status": "ok",
    "_index": "twitter",
    "_type": "tweet",
    "_id": "1",
    "_version": 1
}
```
NOTE: A missing document will always be created (`upsert` mode) because the `op_type` parameter is not implemented yet (http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/docs-index_.html).


### Get

http://www.elasticsearch.org/guide/reference/api/get/

An example would be:

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    elasticSearchService.get("twitter", "tweet", "123", jsonResult -> {
        // Do something
    });
    
    final GetOptions getOptions = new GetOptions()
        .setFetchSource(true)
        .addField("id")
        .addField("message");
        // etc.
    
    elasticSearchService.get("twitter", "tweet", "123", getOptions, jsonResult -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    rxElasticSearchService.get("twitter", "tweet", "123")
        .subscribe(jsonResult -> {
            // Do something
        });
        
    final GetOptions getOptions = new GetOptions()
        .setFetchSource(true)
        .addField("id")
        .addField("message");
        // etc.
        
    rxElasticSearchService.get("twitter", "tweet", "123", getOptions)
        .subscribe(jsonResult -> {
            // Do something
        });
```

The event bus replies with a json message with the following structure:

```json
{
    "status": <status>,
    "_index": <index>,
    "_type": <type>,
    "_id": <id>,
    "_version": <version>
    "_source": <source>
}
```

* `status` - either `ok` or `error`
* `index` - the index name.
* `type` - the type name.
* `id` - the string id of the source to insert/update.  This is optional, if missing a new id will be generated by elastic search and returned.
* `version` - the numeric version of the source document starting at 1.
* `source` - the source json document to index

An example message would be:

```json
{
    "status": "ok",
    "_index": "twitter",
    "_type": "tweet",
    "_id": "1",
    "_version": 1
    "_source": {
        "user": "hubrick",
        "message": "love elastic search!"
    }
}
```


### Search

http://www.elasticsearch.org/guide/reference/api/search/

http://www.elasticsearch.org/guide/reference/query-dsl/

An example message would be:

```java
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    
    elasticSearchService.search("twitter", jsonResult -> {
        // Do something
    });
    
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.SCAN)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, Collections.emptyMap(), SortOrder.DESC);
        // etc.
        
    elasticSearchService.search("twitter", searchOptions, jsonResult -> {
        // Do something
    }); 
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    rxElasticSearchService.search("twitter")
        .subscribe(jsonResult -> {
            // Do something
        });
        
    final SearchOptions searchOptions = new SearchOptions()
        .setQuery(new JsonObject("{\"match_all\": {}}"))
        .setSearchType(SearchType.SCAN)
        .setFetchSource(true)
        .addFieldSort("id", SortOrder.DESC)
        .addScriptSort("...", ScriptSortOption.Type.NUMERIC, Collections.emptyMap(), SortOrder.DESC);
        // etc.
            
    rxElasticSearchService.search("twitter", searchOptions)
        .subscribe(jsonResult -> {
            // Do something
        });
```

The event bus replies with a json message with a status `"ok"` or `"error"` along with the standard elastic search json search response.  See the documentation for details.

An example reply message for the query above would be:

```json
{
    "status": "ok",
    "took" : 3,
    "timed_out" : false,
    "_shards" : {
        "total" : 5,
        "successful" : 5,
        "failed" : 0
    },
    "hits" : {
        "total" : 2,
        "max_score" : 0.19178301,
        "hits" : [
            {
                "_index" : "twitter",
                "_type" : "tweet",
                "_id" : "1",
                "_score" : 0.19178301,
                "_source" : {
                    "user": "hubrick",
                    "message" : "love elastic search!"
                }
            },
            {
                "_index" : "twitter",
                "_type" : "tweet",
                "_id" : "2",
                "_score" : 0.19178301,
                "_source" : {
                    "user": "hubrick",
                    "message" : "still searching away"
                }
            }
        ]
    }
}
```

### Scroll

http://www.elasticsearch.org/guide/reference/api/search/scroll/

First send a search message with `search_type` = `"scan"` and `scroll` = `"5m"` (some time string).  The search result will include a `_scroll_id` that will be valid for the scroll time specified.

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    elasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", jsonResult -> {
        // Do something
    });
    
    final SearchScrollOptions searchScrollOptions = new SearchScrollOptions()
        .setScroll("5m");

    elasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchScrollOptions, jsonResult -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
            
    rxElasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI")
        .subscribe(jsonResult -> {
            // Do something
        });
       
    final SearchScrollOptions searchScrollOptions = new SearchScrollOptions()
        .setScroll("5m");
    
    rxElasticSearchService.searchScroll("c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI", searchScrollOptions)
        .subscribe(jsonResult -> {
            // Do something
        });
}
```

The event bus replies with a json message with a status `"ok"` or `"error"` along with the standard elastic search json scroll response.  See the documentation for details.

An example reply message for the scroll above would be:

```json
{
    "status": "ok",
    "_scroll_id": "c2Nhbjs1OzIxMTpyUkpzWnBIYVMzbVB0VGlaNHdjcWpnOzIxNTpyUkpzWnBI",
    "took": 2,
    "timed_out": false,
    "_shards": {
        "total": 5,
        "successful": 5,
        "failed": 0
    },
    "hits" : {
        "total" : 2,
        "max_score" : 0.0,
        "hits" : [
            {
                "_index" : "twitter",
                "_type" : "tweet",
                "_id" : "1",
                "_score" : 0.0,
                "_source" : {
                    "user": "hubrick",
                    "message" : "love elastic search!"
                }
            },
            {
                "_index" : "twitter",
                "_type" : "tweet",
                "_id" : "2",
                "_score" : 0.0,
                "_source" : {
                    "user": "hubrick",
                    "message" : "still searching away"
                }
            }
        ]
    }
}
```

### Suggest

http://www.elasticsearch.org/guide/reference/api/search/scroll/

First send a search message with `search_type` = `"scan"` and `scroll` = `"5m"` (some time string).  The search result will include a `_scroll_id` that will be valid for the scroll time specified.

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
        
    elasticSearchService.suggest("twitter", jsonResult -> {
        // Do something
    });
    
    final SuggestOptions suggestOptions = new SuggestOptions();
    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
        .setText("the amsterdma meetpu");
        // etc.
    suggestOptions.addSuggestion("my-suggest-1", completionSuggestOption);

    elasticSearchService.suggest("twitter", suggestOptions, jsonResult -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
            
    rxElasticSearchService.suggest("twitter")
        .subscribe(jsonResult -> {
            // Do something
        });
        
    final SuggestOptions suggestOptions = new SuggestOptions();
    final CompletionSuggestOption completionSuggestOption = new CompletionSuggestOption()
       .setText("the amsterdma meetpu");
       // etc.
    suggestOptions.addSuggestion("my-suggest-1", completionSuggestOption);
    
    rxElasticSearchService.suggest("twitter", suggestOptions)
        .subscribe(jsonResult -> {
            // Do something
        });
}
```

The event bus replies with a json message with a status `"ok"` or `"error"` along with the standard elastic search json scroll response.  See the documentation for details.

An example reply message for the scroll above would be:

```json
{
   "suggest" : {
        "my-suggest-1": [
              {
                "text" : "amsterdma",
                "offset": 4,
                "length": 9,
                "options": [
                   ...
                ]
              },
              ...
        ]
   }
}
```

### Delete

http://www.elasticsearch.org/guide/reference/api/delete/

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
           
    elasticSearchService.delete("twitter", "tweet", "123", jsonResult -> {
        // Do something
    });
    
    final DeleteOptions deleteOptions = new DeleteOptions()
        .setTimeout("10s");
        
    elasticSearchService.delete("twitter", "tweet", "123", deleteOptions, jsonResult -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
           
    rxElasticSearchService.delete("twitter", "tweet", "123")
        .subscribe(jsonResult -> {
            // Do something
        });
        
    final DeleteOptions deleteOptions = new DeleteOptions()
        .setTimeout("10s");
            
    rxElasticSearchService.delete("twitter", "tweet", "123", deleteOptions)
        .subscribe(jsonResult -> {
            // Do something
        });;
}
```

The event bus replies with a json message with the following structure:

```json
{
    "found": <status>,
    "_index": <index>,
    "_type": <type>,
    "_id": <id>,
    "_version": <version>
}
```

* `found` - either `true` or `false`
* `index` - the index name.
* `type` - the type name.
* `id` - the string id of the source to delete.
* `version` - the numeric version of the deleted document starting at 1.

An example message would be:

```json
{
    "found": "true",
    "_index": "twitter",
    "_type": "tweet",
    "_id": "1",
    "_version": 1
}
```


## Supported Plugins
### Delete By Query

https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete-by-query.html

An example message would be:

```java
{
    // Plain
    final ElasticSearchService elasticSearchService = ElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    final JsonObject query = new JsonObject("{\"match_all\": {}}");
           
    elasticSearchService.deleteByQuery("twitter", query, jsonResult -> {
        // Do something
    });
    
    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
        .setTimeout("10s");
        
    elasticSearchService.delete("twitter", query, deleteByQueryOptions, jsonResult -> {
        // Do something
    });
    
    
    // RxJava
    final RxElasticSearchService rxElasticSearchService = RxElasticSearchService.createEventBusProxy(vertx, "eventbus-address");
    final JsonObject query = new JsonObject("{\"match_all\": {}}");
              
    rxElasticSearchService.deleteByQuery("twitter", query)
        .subscribe(jsonResult -> {
            // Do something
        });
      
    final DeleteByQueryOptions deleteByQueryOptions = new DeleteByQueryOptions()
        .setTimeout("10s");
            
    rxElasticSearchService.delete("twitter", query, deleteByQueryOptions)
        .subscribe(jsonResult -> {
            // Do something
        });
}
```

The event bus replies with a json message with the following structure:

```json
{
    "took": <time>,
    "timed_out": <boolean>,
    "_indices": <indices>
}
```

* `took` - the time it took
* `timed_out` - either `true` or `false`
* `_indices` - involved indices

An example message would be:

```json
{
    "took" : 639,
    "timed_out" : false,
    "_indices" : {
        "_all" : {
            "found" : 5901,
            "deleted" : 5901,
            "missing" : 0,
            "failed" : 0
        },
        "twitter" : {
            "found" : 5901,
            "deleted" : 5901,
            "missing" : 0,
            "failed" : 0
        }
    },
    "failures" : [ ]
}
```
