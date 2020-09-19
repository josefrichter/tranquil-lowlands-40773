(ns webdev.core
  (:require [webdev.item.model :as items]
            [webdev.item.handler :refer [handle-index-items
                                         handle-create-item
                                         handle-read-item
                                         handle-update-item
                                         handle-delete-item]])
  (:require [ring.adapter.jetty :as jetty]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.resource :refer [wrap-resource]]
            [ring.middleware.file-info :refer [wrap-file-info]]
            [compojure.core :refer [defroutes ANY GET POST PUT DELETE]]
            [compojure.route :refer [not-found]]
            [ring.handler.dump :refer [handle-dump]]))

(def db (or 
         (System/getenv "DATABASE_URL")
         "jdbc:postgresql://localhost/webdev"))

(def myserver "myserver")

(defroutes routes
  (GET "/items" [] handle-index-items)
  (POST "/items" [] handle-create-item)
  (GET "/items/:item-id" [] handle-read-item)
  (PUT "/items/:item-id" [] handle-update-item)
  (DELETE "/items/:item-id" [] handle-delete-item)

  (not-found "Page not found."))

(defn wrap-server [hdlr]
  (fn [req]
    (assoc-in (hdlr req) [:headers "Server"] myserver)))

(defn wrap-db [hdlr]
  (fn [req]
    (hdlr (assoc req :webdev/db db))))

(def sim-methods {"PUT" :put
                  "DELETE" :delete})

;; this is necessary to simulate PUT and DELETE
;; because they're not suppored by browsers
;; so we put them as hidden fields to forms
(defn wrap-simulated-methods [hdlr]
  (fn [req]
    (if-let [method (and (= :post (:request-method req))
                         (sim-methods (get-in req [:params "_method"])))]
      (hdlr (assoc req :request-method method))
      (hdlr req))))

(def app
  (-> routes
      wrap-simulated-methods
      wrap-params
      wrap-db
      (wrap-resource "static")
      wrap-file-info
      wrap-server))

(defn -main [port]
  (items/create-table db)
  (jetty/run-jetty app {:port (Integer. port)}))

(defn -dev-main [port]
  (items/create-table db)
  (jetty/run-jetty (wrap-reload #'app) {:port (Integer. port)}))