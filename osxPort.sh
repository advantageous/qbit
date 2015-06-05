#!/usr/bin/env bash
sudo lsof -iTCP -sTCP:LISTEN -n -P
