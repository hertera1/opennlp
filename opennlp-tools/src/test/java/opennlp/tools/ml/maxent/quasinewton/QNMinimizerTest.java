/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.ml.maxent.quasinewton;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class QNMinimizerTest {

  @Test
  void testQuadraticFunction() {
    QNMinimizer minimizer = new QNMinimizer();
    Function f = new QuadraticFunction();
    double[] x = minimizer.minimize(f);
    double minValue = f.valueAt(x);

    Assertions.assertEquals(1.0, x[0], 1e-5);
    Assertions.assertEquals(5.0, x[1], 1e-5);
    Assertions.assertEquals(10.0, minValue, 1e-10);
  }

  @Test
  void testRosenbrockFunction() {
    QNMinimizer minimizer = new QNMinimizer();
    Function f = new Rosenbrock();
    double[] x = minimizer.minimize(f);
    double minValue = f.valueAt(x);

    Assertions.assertEquals(1.0, x[0], 1e-5);
    Assertions.assertEquals(1.0, x[1], 1e-5);
    Assertions.assertEquals(0, minValue, 1e-10);
  }

  /**
   * Quadratic function: f(x,y) = (x-1)^2 + (y-5)^2 + 10
   */
  public class QuadraticFunction implements Function {

    @Override
    public int getDimension() {
      return 2;
    }

    @Override
    public double valueAt(double[] x) {
      return StrictMath.pow(x[0] - 1, 2) + StrictMath.pow(x[1] - 5, 2) + 10;
    }

    @Override
    public double[] gradientAt(double[] x) {
      return new double[] {2 * (x[0] - 1), 2 * (x[1] - 5)};
    }
  }

  /**
   * Rosenbrock function (http://en.wikipedia.org/wiki/Rosenbrock_function)
   * f(x,y) = (1-x)^2 + 100*(y-x^2)^2
   * f(x,y) is non-convex and has global minimum at (x,y) = (1,1) where f(x,y) = 0
   * <p>
   * f_x = -2*(1-x) - 400*(y-x^2)*x
   * f_y = 200*(y-x^2)
   */
  public class Rosenbrock implements Function {

    @Override
    public int getDimension() {
      return 2;
    }

    @Override
    public double valueAt(double[] x) {
      return StrictMath.pow(1 - x[0], 2) + 100 * StrictMath.pow(x[1] - StrictMath.pow(x[0], 2), 2);
    }

    @Override
    public double[] gradientAt(double[] x) {
      double[] g = new double[2];
      g[0] = -2 * (1 - x[0]) - 400 * (x[1] - StrictMath.pow(x[0], 2)) * x[0];
      g[1] = 200 * (x[1] - StrictMath.pow(x[0], 2));
      return g;
    }

  }
}
