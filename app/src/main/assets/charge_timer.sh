#!/usr/bin/env bash

if [[ -e /sys/class/power_supply/battery/safety_timer_enabled ]]
then
    if [[ `cat /sys/class/power_supply/battery/safety_timer_enabled` == 1 ]]
    then
        X=0
    else
        X=1
    fi
    echo  ${X} > /sys/class/power_supply/battery/safety_timer_enabled
fi