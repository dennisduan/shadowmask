/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.shadowmask.engine.spark.autosearch.pso;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.spark.rdd.RDD;
import org.shadowmask.core.util.Rethrow;
import org.shadowmask.engine.spark.autosearch.Executable;
import org.shadowmask.engine.spark.autosearch.Segment;

public abstract class MkFitnessCalculator<TABLE> {

  private int threadNums;

  private ExecutorService executor;

  public MkFitnessCalculator(int threadNums) {
    this.threadNums = threadNums;
    executor = Executors.newFixedThreadPool(threadNums);
  }

  public void shutdownExecutor(){
    this.executor.shutdown();
  }

  public void calculateFitness(final List<MkParticle> particles,
      final Map<MkParticle, MkFitness> fitnessMap, final Object waitObject,
      final TABLE dataSet) {

    final AtomicInteger count = new AtomicInteger(0);
    for (final MkParticle particle : particles) {
      final Executable executable = new Executable() {
        @Override public void exe() {
          fitnessMap.put(particle, calculateOne(particle, dataSet));
        }
      };
      executable.registerFinallySegment(new Segment() {
        @Override public void run() {
          synchronized (waitObject){
            count.incrementAndGet();
            if (count.get() == particles.size()) {
              waitObject.notify();
            }
          }
        }

        @Override public void attach(Object o) {
          // do nothing
        }
      });
      executable.registerExceptionSegment(new Segment<Exception>() {
        @Override public void run() {
          // do nothing
        }

        @Override public void attach(Exception e) {
          e.printStackTrace();
          Rethrow.rethrow(e);
        }
      });
      this.executor.submit(executable);
    }
  }

  public abstract MkFitness calculateOne(MkParticle particle, TABLE dataSet) ;
}
