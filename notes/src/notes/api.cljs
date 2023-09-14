(ns notes.api
  (:require
   [central :as central]))

(defn json [obj] (js/JSON.stringify (clj->js obj)))

(defn token [] (central/Users.token))

(defn request [url options]
  (->
    (central/Api.Blob.request url (clj->js options))
    (.then (fn [res] (.json res)))
    (.then #(js->clj %))))

(defn central-request [url options]
  (->
    (central/Api.Central.request url (clj->js options))
    (.then (fn [res] (.json res)))
    (.then #(js->clj %))))

(defn get-user []
  (->
    (central-request "/users/validate" {:method "POST" :body (json {:token (token)})})
    (.then #(:users %))))

(defn list-notes [email]
  (->
    (request "/blobs/list" {:method "POST" :body (json {:bucket email :prefix "notes/"})})
    (.then #(:blobs %))))
