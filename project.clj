(defproject ops-game "1.0.0-SNAPSHOT"
  :description "Operational Game for CMBN"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [seesaw "1.0.6"]
                 [processing "1.5.1"]
                 [lwjgl "2.7.1"]
                 [lessvoid/nifty "1.3"]
                 [lessvoid/nifty-default-controls "1.3"]
                 [lessvoid/nifty-style-black "1.3"]
                 [lessvoid/nifty-lwjgl-renderer "1.3" :exclusions [lwjgl/lwjgl lwjgl/jinput lwjgl/lwjgl-util]]
                 [lessvoid/nifty-openal-soundsystem "1.0"]
                 [slick/slick "b274"]
                 ]
  :dev-dependencies [[marginalia "0.5.1"]
                     [clojure-source "1.2.0"]]
  :repositories {"nifty" "http://nifty-gui.sourceforge.net/nifty-maven-repo"}
  :jvm-opts ["-Djava.library.path=/Users/yair/Downloads/lwjgl-2.7.1/native/macosx"
             "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"
             ])

