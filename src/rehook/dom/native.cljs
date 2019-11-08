(ns rehook.dom.native
  (:require
   ["react" :as react]
   ["react-native" :as rn]
   [rehook.util :as util]))

(defn handle-type
  [e ctx $]
  (cond
    (keyword? e)
    (aget rn (name e))

    (util/rehook-component? e)
    (e ctx $)

    (sequential? e)
    (apply react/Fragment e)

    :else e))

(defn bootstrap
  ([ctx ctx-f props-f e]
   (let [ctx (ctx-f ctx e)]
     (react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f props-f)))))
  ([ctx ctx-f props-f e args]
   (let [ctx (ctx-f ctx e)]
     (react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f props-f)) (clj->js args))))
  ([ctx ctx-f props-f e args & children]
   (let [ctx (ctx-f ctx e)]
     (apply react/createElement (handle-type e ctx (partial bootstrap ctx ctx-f props-f)) (clj->js args) children))))

(defn component-provider
  ([ctx component]
   (component-provider ctx identity clj->js component))
  ([ctx ctx-f props-f component]
   (constantly #(react/createElement (bootstrap ctx ctx-f props-f component)))))