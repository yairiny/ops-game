(defproject ops-game "1.0.0-SNAPSHOT"
  :description "Operational Game for CMBN"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [seesaw "1.0.6"]
                 [processing "1.5.1"]
                 [lwjgl "2.7.1"]]
  :dev-dependencies [[marginalia "0.5.1"]
                     [clojure-source "1.2.0"]]
  :jvm-opts ["-Djava.library.path=/Users/yair/Downloads/lwjgl-2.7.1/native/macosx"
             "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n"
             ])

