(ns notes.components.menu
  (:require
    [re-frame.core :as re-frame]
    [spade.core :refer [defclass]]
    [central :as central]))

(defclass pane-style []
  {:position "fixed"
   :display "flex"
   :align-items "center"
   :background-color "white"
   :width "100%"
   :height "50px"
   :padding-left "10px"
   :padding-right "10px"
   :border-bottom (str "1px solid" central/Constants.colors.black)
   :box-shadow (str "0px 0px 1px" central/Constants.colors.lightBlack)
   :justify-content "space-between"})
(defn pane [children] (into [:div {:class (pane-style)}] children))

(defclass title-style [] {:font-size "20px" :color central/Constants.colors.black})
(defn title [children] (into [:div {:class (title-style)}] children))

; TODO: JK delete button
(defn build [selected]
  (pane [(title [(:title selected)])]))
