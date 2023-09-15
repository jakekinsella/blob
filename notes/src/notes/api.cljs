(ns notes.api
  (:require
    [clojure.string :as string]
    [central :as central]))

(defn json [obj] (js/JSON.stringify (clj->js obj)))

(defn token [] (central/Users.token))

(defn request [url options]
  (->
    (central/Api.Blob.request url (clj->js options))
    (.then (fn [res] (.json res)))
    (.then #(js->clj % :keywordize-keys true))))

(defn central-request [url options]
  (->
    (central/Api.Central.request url (clj->js options))
    (.then (fn [res] (.json res)))
    (.then #(js->clj % :keywordize-keys true))))

(defn get-user []
  (->
    (central-request "/users/validate" {:method "POST" :body (json {:token (token)})})
    (.then #(:user %))))

(defn note-apply [note]
  {:title (string/replace (:key note) #"^notes/" "") :body (:body note)})

(defn list-notes [email]
  (->
    (request "/blobs/list" {:method "POST" :body (json {:bucket email :prefix "notes/"})})
    (.then #(:blobs %))
    (.then #(map note-apply %))))
