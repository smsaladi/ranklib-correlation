#!/bin/bash

mkdir -p lib
cd lib
wget http://apache.mirrors.pair.com//commons/math/binaries/commons-math3-3.6.1-bin.tar.gz
tar xzf commons-math3-3.6.1-bin.tar.gz
mv commons-math3-3.6.1/commons-math3-3.6.1.jar ./
rm -r commons-math3-3.6.1-bin.tar.gz commons-math3-3.6.1

