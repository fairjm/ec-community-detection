package com.cc.graph.util

import java.util.concurrent.locks.Lock

object LockUtil {

  def withLock[T](code: => T)(implicit lock: Lock): T = {
    lock.lock()
    try {
      code
    } finally { lock.unlock() }
  }

}