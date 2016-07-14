(ns bonjure.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [bonjure.core-test]))

(doo-tests 'bonjure.core-test)

