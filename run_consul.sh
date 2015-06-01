#!/bin/bash
consul agent -server -bootstrap-expect 1 -dc dc1 -data-dir /tmp/consulqbit -ui-dir ./support/opt/rdio/consul/web/
