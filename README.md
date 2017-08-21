Elasticsearch DynaRank Plugin
=======================

## Overview

DynaRank Plugin provides a feature for Dynamic Ranking at a search time.
You can change top N documents in the search result with your re-ordering algorithm.
Elasticsearch has [rescoring](http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/search-request-rescore.html "rescoring"), but DynaRank is different as below:

 * DynaRank's reranking is executed on requested node only, not on each shard. 
 * DynaRank uses a script language for reranking.


## Version

| Version   | Elasticsearch |
|:---------:|:-------------:|
| master    | 5.5.X         |
| 5.5.1     | 5.5.2         |
| 2.4.0     | 2.4.0         |
| 2.3.1     | 2.3.5         |
| 2.2.0     | 2.2.0         |
| 2.1.0     | 2.1.1         |
| 1.6.0     | 1.6.0         |
| 1.5.1     | 1.5.2         |
| 1.4.5     | 1.4.2         |
| 1.3.0     | 1.3.2         |

### Issues/Questions

Please file an [issue](https://github.com/codelibs/elasticsearch-dynarank/issues "issue").
(Japanese forum is [here](https://github.com/codelibs/codelibs-ja-forum "here").)

## Installation

### For 5.x

    $ $ES_HOME/bin/elasticsearch-plugin install org.codelibs:elasticsearch-dynarank:5.5.1

### For 2.x

    $ $ES_HOME/bin/plugin install org.codelibs/elasticsearch-dynarank/2.4.0

## Getting Started

### Create Sample Data

Create "sample" index:

    $ COUNT=1;while [ $COUNT -le 100 ] ; do curl -XPOST 'localhost:9200/sample/data' -d "{\"message\":\"Hello $COUNT\",\"counter\":$COUNT}";COUNT=`expr $COUNT + 1`; done

100 documents are inserted. You can see 10 documents by an ascending order of "counter" field:

    $ curl -XPOST "http://127.0.0.1:9200/sample/data/_search" -d'
    {
       "query": {
          "match_all": {}
       },
       "fields": [
          "counter",
          "_source"
       ],
       "sort": [
          {
             "counter": {
                 "order": "asc"
             }
          }
       ]
    }'

### Enable Reranking

DynaRank plugin is enabled if your re-order script is set to the target index:

    $ curl -XPUT 'localhost:9200/sample/_settings?index.dynarank.script_sort.script=searchHits.sort%20%7Bs1%2C%20s2%20-%3E%20s2.getSource%28%29.get%28%27counter%27%29%20-%20s1.getSource%28%29.get%28%27counter%27%29%7D%20as%20org.elasticsearch.search.SearchHit%5B%5D'
    $ curl -XPUT 'localhost:9200/sample/_settings?index.dynarank.reorder_size=5'

The above script is:

    searchHits.sort {s1, s2 -> s2.getSource().get('counter').value() - s1.getSource().get('counter').value()} as org.elasticsearch.search.SearchHit[]

This setting sorts top 5 documents (5 is given by reorder\_size) by a descending order of "counter" field, and others are by an ascending order.

### Disable Reranking

Set an empty value to index.dynarank.script\_sort.script:

    $ curl -XPUT 'localhost:9200/sample/_settings?index.dynarank.script_sort.script='

## References

### dynarank\_diversity\_sort Script Sort

DynaRank plugin provides a sort feature for a diversity problem.
The sort script is dynarank\_diversity\_sort.
The configuration is below:

    curl -XPUT 'localhost:9200/sample/_settings' -d '
    {
      "index" : {
        "dynarank":{
          "script_sort":{
            "lang":"dynarank_diversity_sort",
            "params":{
              "diversity_fields":["filedname1", "filedname2"],
              "diversity_thresholds":[0.95, 1]
            }
          },
          "reorder_size":100
         }
      }
    }'

diversity\_fields is fields for a diversity.
diversity\_thresholds is a threshold for a similarity of each document.
