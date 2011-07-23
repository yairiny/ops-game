(ns ops-game.gui.save-and-load
  (:require [ops-game.opengl.nifty :as nifty]))

(def ^{:private true :doc "holds state for the UI"} ui-state
  (atom {:dialog-id nil :callback-fn nil}))

(defn- close-dialog "closes the given dialog"
  [nifty dialog-id] (.closePopup nifty dialog-id))

(defn initialise-dialogs
  "initialises the event handlers for the dialogs"
  [nifty]
  
  (nifty/subscribe-event nifty "load-dialog-cancel-button" (fn [_ _] (close-dialog))))

(defn show-load-dialog
  "shows the load dialog"
  [nifty callback-fn]
  {:pre [nifty (fn? callback-fn)]}
  (let [id (nifty/show-popup nifty "load-popup")]
    (nifty/subscribe-event nifty "load-dialog-ok-button"
                         (fn [_ _]
                           (callback-fn true)
                           (close-dialog nifty id)))
    (nifty/subscribe-event nifty "load-dialog-cancel-button"
                         (fn [_ _]
                           (callback-fn false)
                           (close-dialog nifty id)))))
