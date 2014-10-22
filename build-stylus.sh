#!/bin/bash
#
# compiles stylus
# executes watch/compile if first argument is "watch"

CMD=${1:-""}

if [[ $CMD = "watch" ]]
then
  stylus -u nib -w blog/styl/main.styl blog/styl/mobile.styl -o blog/public/css
else
  stylus -u nib blog/styl/main.styl -o blog/public/css
  stylus -u nib blog/styl/mobile.styl -o blog/public/css
fi
