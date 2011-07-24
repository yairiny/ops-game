(ns ops-game.gui.save-and-load
  (:require [ops-game.opengl.nifty :as nifty]
            [ops-game.data.persistence :as pst]))

(defn- close-dialog "closes the given dialog"
  [nifty dialog-id] (.closePopup nifty dialog-id))

(defn initialise-dialogs
  "initialises the event handlers for the dialogs"
  [nifty])

;;TODO use a macro to commonify the idiom of calling the callback-fn
;;with different values based on which button is clicked and then
;;closing the dialog

(defn show-load-dialog
  "shows the load dialog"
  [nifty callback-fn]
  {:pre [nifty (fn? callback-fn)]}
  (let [id (nifty/show-popup nifty "load-popup")]
    (nifty/add-items-to-list nifty id "load-dialog-list" (pst/get-save-files))
    (nifty/subscribe-event nifty "load-dialog-ok-button"
                           (fn [_ _]
                             (callback-fn (first (nifty/get-selected-items nifty id "load-dialog-list")))
                             (close-dialog nifty id))
                           :button-clicked)
    (nifty/subscribe-event nifty "load-dialog-cancel-button"
                           (fn [_ _]
                             (callback-fn nil)
                             (close-dialog nifty id))
                           :button-clicked)))

(defn show-save-dialog 
  "shows the save dialog"
  [nifty callback-fn] 
  {:pre [nifty (fn? callback-fn)]}
  (let [id (nifty/show-popup nifty "save-popup")]
    (nifty/add-items-to-list nifty id "save-dialog-list" (pst/get-save-files))
    (nifty/subscribe-event nifty "save-dialog-list"
                           (fn [_ evt]
                             (let [filename (first (.getSelection evt))]
                               (nifty/update-field-text nifty id "save-dialog-text" filename)))
                           :listbox-selection-changed)
    (nifty/subscribe-event nifty "save-dialog-ok-button"
                           (fn [_ _]
                             (callback-fn (nifty/get-field-text nifty id "save-dialog-text"))
                             (close-dialog nifty id))
                           :button-clicked)
    (nifty/subscribe-event nifty "save-dialog-cancel-button"
                           (fn [_ _]
                             (callback-fn nil)
                             (close-dialog nifty id))
                           :button-clicked)))

(defn show-confirm-dialog 
  "shows a confirmation dialog with a message"
  [nifty message callback-fn] 
  {:pre [nifty (string? message) (fn? callback-fn)]}
  (let [id (nifty/show-popup nifty "confirm-popup")]
    (nifty/update-label-text nifty id "confirm-dialog-message" message)
    (nifty/subscribe-event nifty "confirm-dialog-ok-button"
                           (fn [_ _]
                             (callback-fn true)
                             (close-dialog nifty id))
                           :button-clicked)
    (nifty/subscribe-event nifty "confirm-dialog-cancel-button"
                           (fn [_ _]
                             (callback-fn false)
                             (close-dialog nifty id))
                           :button-clicked)))
