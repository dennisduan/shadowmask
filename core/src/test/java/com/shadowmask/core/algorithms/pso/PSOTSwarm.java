package com.shadowmask.core.algorithms.pso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.shadowmask.core.algorithms.pso.Swarm;

public class PSOTSwarm
    extends Swarm<PSOTVelocity, PSOTFitness, PSOTPosition, PSOTParticle> {

  List<PSOTParticle> particles;

  public PSOTSwarm() {
    init();
  }

  private void init() {
    particles = new ArrayList<>(particleSize());
    for (int i = 0; i < particleSize(); ++i) {
      particles.add(new PSOTParticle(this));
    }
  }

  @Override public List<PSOTParticle> particles() {
    return particles;
  }

  @Override public Map<PSOTParticle, PSOTFitness> calculateFitness() {
    if (fitnessMap != null) {
      fitnessMap.clear();
    }else {
      this.fitnessMap = new HashMap<>();
    }
    // may be parallelization
    PSOTFitnessCalculator calculator = new PSOTFitnessCalculator();
    for (PSOTParticle particle : particles) {
      fitnessMap.put(particle, calculator.fitness(particle));
    }
    return fitnessMap;
  }

  @Override public Map<PSOTParticle, PSOTVelocity> calculateNewVelocities() {
    if (newVelocities != null) {
      newVelocities.clear();
    } else {
      this.newVelocities = new HashMap<>();
    }
    PSOTVelocityCalculator calculator = new PSOTVelocityCalculator() {
      @Override public double randomSearchRate() {
        return 0.01D;
      }

      @Override public double lBound() {
        return -999999999D;
      }

      @Override public double hBound() {
        return 999999999D;
      }
    };
    for (PSOTParticle particle : particles) {
      newVelocities.put(particle, calculator
          .newVelocity(particle.currentVelocity(), particle.currentPosition(),
              particle.currentFitness(), particle.historyBestPosition(),
              particle.historyBestFitness(),
              globalBestParticle().historyBestPosition(),
              globalBestParticle().historyBestFitness(),
              currentBestParticle().currentPosition(),
              currentBestParticle().currentFitness(),
              currentWorstParticle().currentPosition(),
              currentWorstParticle().currentFitness()));
    }
    return newVelocities;
  }

  @Override public int maxSteps() {
    return 300;
  }

  @Override public int particleSize() {
    return 200;
  }

  @Override public void updateCurrentBestParticle(PSOTParticle p) {
    super.updateCurrentBestParticle(p);
    //    if(p !=null) {
    //      System.out.println(p.currentPosition.xValue+"\t"+p.currentFitness().value);
    //    }
  }


  @Override public void updateGlobalBestParticle(PSOTParticle p) {

    super.updateGlobalBestParticle(p);
    if (p != null) {
      System.out.println(
          p.historyBestPosition.xValue + "\t" + p.historyBestFitness.value
              + "\t" + p.hashCode()+"\t");
    }
  }
}
