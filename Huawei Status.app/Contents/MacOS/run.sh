#!/bin/sh
CURRENT_DIR=`dirname "$0"`
type java
if [ $? -eq 0 ]; then
    java -Xmx32M -Dapple.awt.UIElement=true -jar "$CURRENT_DIR/../Java/huawei-status.jar"
else
    ICON_DIR="${CURRENT_DIR//\//:}:..:Resources:Huawei Status.icns"
    osascript << EOF
        tell app "System Events" to display dialog "Install Java to run this application." buttons {"OK"} with title "Huawei Status" with icon file "$ICON_DIR"
EOF
fi