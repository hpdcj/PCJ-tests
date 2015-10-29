/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.moldyn;

import org.pcj.PCJ;

/**
 *
 * @author faramir
 */
public class MolDyn {

    public static final int ITERS = 100;
    public static final double LENGTH = 50e-10;
    public static final double m = 4.0026;
    public static final double mu = 1.66056e-27;
    public static final double kb = 1.38066e-23;
    public static final double TSIM = 50;
    public static final double deltat = 5e-16;
    public Particle one[] = null;
    public double epot = 0.0;
    public double vir = 0.0;
    public double count = 0.0;
    int size;
    int datasizes[] = {8, 13, 16, 24};
    public long interactions = 0;
    int i, j, k, lg, mdsize, move, mm;
    double l, rcoff, rcoffs, side, sideh, hsq, hsq2, vel;
    double a, r, sum, tscale, sc, ekin, ek, ts, sp;
    double den = 0.83134;
    double tref = 0.722;
    double h = 0.064;
    double vaver, vaverh, rand;
    double etot, temp, pres, rp;
    double u1, u2, v1, v2, s;
    int ijk, npartm, PARTSIZE, iseed, tint;
    int irep = 10;
    int istop = 19;
    int iprint = 10;
    int movemx = 50;
    random randnum;

    public void initialise() {

        /* Parameter determination */
        mm = datasizes[size];
        PARTSIZE = mm * mm * mm * 4;
        mdsize = PARTSIZE;
        one = new Particle[mdsize];
        l = LENGTH;

        side = Math.pow((mdsize / den), 0.3333333);
        rcoff = mm / 4.0;

        a = side / mm;
        sideh = side * 0.5;
        hsq = h * h;
        hsq2 = hsq * 0.5;
        npartm = mdsize - 1;
        rcoffs = rcoff * rcoff;
        tscale = 16.0 / (1.0 * mdsize - 1.0);
        vaver = 1.13 * Math.sqrt(tref / 24.0);
        vaverh = vaver * h;

        /* Particle Generation */
        ijk = 0;
        for (lg = 0; lg <= 1; lg++) {
            for (i = 0; i < mm; i++) {
                for (j = 0; j < mm; j++) {
                    for (k = 0; k < mm; k++) {
                        one[ijk] = new Particle(this, (i * a + lg * a * 0.5), (j * a + lg * a * 0.5), (k * a),
                                0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                        ijk = ijk + 1;
                    }
                }
            }
        }
        for (lg = 1; lg <= 2; lg++) {
            for (i = 0; i < mm; i++) {
                for (j = 0; j < mm; j++) {
                    for (k = 0; k < mm; k++) {
                        one[ijk] = new Particle(this, (i * a + (2 - lg) * a * 0.5), (j * a + (lg - 1) * a * 0.5),
                                (k * a + a * 0.5), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
                        ijk = ijk + 1;
                    }
                }
            }
        }

        /* Initialise velocities */
        iseed = 0;
        v1 = 0.0;
        v2 = 0.0;

        randnum = new random(iseed, v1, v2);

        for (i = 0; i < mdsize; i += 2) {
            r = randnum.seed();
            one[i].xvelocity = r * randnum.v1;
            one[i + 1].xvelocity = r * randnum.v2;
        }

        for (i = 0; i < mdsize; i += 2) {
            r = randnum.seed();
            one[i].yvelocity = r * randnum.v1;
            one[i + 1].yvelocity = r * randnum.v2;
        }

        for (i = 0; i < mdsize; i += 2) {
            r = randnum.seed();
            one[i].zvelocity = r * randnum.v1;
            one[i + 1].zvelocity = r * randnum.v2;
        }

        /* velocity scaling */
        ekin = 0.0;
        sp = 0.0;

        for (i = 0; i < mdsize; i++) {
            sp = sp + one[i].xvelocity;
        }
        sp = sp / mdsize;

        for (i = 0; i < mdsize; i++) {
            one[i].xvelocity = one[i].xvelocity - sp;
            ekin = ekin + one[i].xvelocity * one[i].xvelocity;
        }

        sp = 0.0;
        for (i = 0; i < mdsize; i++) {
            sp = sp + one[i].yvelocity;
        }
        sp = sp / mdsize;

        for (i = 0; i < mdsize; i++) {
            one[i].yvelocity = one[i].yvelocity - sp;
            ekin = ekin + one[i].yvelocity * one[i].yvelocity;
        }

        sp = 0.0;
        for (i = 0; i < mdsize; i++) {
            sp = sp + one[i].zvelocity;
        }
        sp = sp / mdsize;

        for (i = 0; i < mdsize; i++) {
            one[i].zvelocity = one[i].zvelocity - sp;
            ekin = ekin + one[i].zvelocity * one[i].zvelocity;
        }

        ts = tscale * ekin;
        sc = h * Math.sqrt(tref / ts);

        for (i = 0; i < mdsize; i++) {

            one[i].xvelocity = one[i].xvelocity * sc;
            one[i].yvelocity = one[i].yvelocity * sc;
            one[i].zvelocity = one[i].zvelocity * sc;

        }

        /* MD simulation */
    }

    public void runiters(int rank, int nprocess) {
        move = 0;
        for (move = 0; move < movemx; move++) {

            for (i = 0; i < mdsize; i++) {
                one[i].domove(side);        /* move the particles and update velocities */

            }

            epot = 0.0;
            vir = 0.0;

            for (i = 0 + rank; i < mdsize; i += nprocess) {
                one[i].force(side, rcoff, mdsize, i);  /* compute forces */

            }

            /* global reduction on partial sums of the forces, epot, vir and interactions */
            double[] tmp_xforce = new double[mdsize];
            double[] tmp_yforce = new double[mdsize];
            double[] tmp_zforce = new double[mdsize];

            for (i = 0; i < mdsize; i++) {
                tmp_xforce[i] = one[i].xforce;
                tmp_yforce[i] = one[i].yforce;
                tmp_zforce[i] = one[i].zforce;
            }

            // all reduce
//            MPI.COMM_WORLD.Allreduce(tmp_xforce, 0, tmp_xforce, 0, mdsize, MPI.DOUBLE, MPI.SUM);
//            MPI.COMM_WORLD.Allreduce(tmp_yforce, 0, tmp_yforce, 0, mdsize, MPI.DOUBLE, MPI.SUM);
//            MPI.COMM_WORLD.Allreduce(tmp_zforce, 0, tmp_zforce, 0, mdsize, MPI.DOUBLE, MPI.SUM);
            if (PCJ.myId() != 0) {
                PCJ.put(0, "r_epot", epot, PCJ.myId());
                PCJ.put(0, "r_vir", vir, PCJ.myId());
                PCJ.put(0, "r_interactions", interactions, PCJ.myId());

                PCJ.put(0, "r_xforce", tmp_xforce, PCJ.myId());
                PCJ.put(0, "r_yforce", tmp_yforce, PCJ.myId());
                PCJ.put(0, "r_zforce", tmp_zforce, PCJ.myId());
            } else {
                PCJ.waitFor("r_epot", PCJ.threadCount() - 1);
                PCJ.waitFor("r_vir", PCJ.threadCount() - 1);
                PCJ.waitFor("r_interactions", PCJ.threadCount() - 1);

                double[] r_epot = PCJ.getLocal("r_epot");
                double[] r_vir = PCJ.getLocal("r_vir");
                long[] r_interactions = PCJ.getLocal("r_interactions");
                for (int node = 1; node < PCJ.threadCount(); ++node) {
                    epot += r_epot[node];
                    vir += r_vir[node];
                    interactions += r_interactions[node];
                }
                PCJ.broadcast("tmp_epot", epot);
                PCJ.broadcast("tmp_vir", vir);
                PCJ.broadcast("tmp_interactions", interactions);

                PCJ.waitFor("r_xforce", PCJ.threadCount() - 1);
                PCJ.waitFor("r_yforce", PCJ.threadCount() - 1);
                PCJ.waitFor("r_zforce", PCJ.threadCount() - 1);

                double[][] r_xforce = PCJ.getLocal("r_xforce");
                double[][] r_yforce = PCJ.getLocal("r_yforce");
                double[][] r_zforce = PCJ.getLocal("r_zforce");

                for (int node = 1; node < PCJ.threadCount(); ++node) {
                    for (i = 0; i < mdsize; ++i) {
                        tmp_xforce[i] += r_xforce[node][i];
                        tmp_yforce[i] += r_yforce[node][i];
                        tmp_zforce[i] += r_zforce[node][i];
                    }
                }
                PCJ.broadcast("tmp_xforce", tmp_xforce);
                PCJ.broadcast("tmp_yforce", tmp_yforce);
                PCJ.broadcast("tmp_zforce", tmp_zforce);
            }

            PCJ.waitFor("tmp_epot");
            PCJ.waitFor("tmp_vir");
            PCJ.waitFor("tmp_interactions");

            epot = PCJ.getLocal("tmp_epot");
            vir = PCJ.getLocal("tmp_vir");
            interactions = PCJ.getLocal("tmp_interactions");
            
            
            PCJ.waitFor("tmp_xforce");
            PCJ.waitFor("tmp_yforce");
            PCJ.waitFor("tmp_zforce");

            tmp_xforce = PCJ.getLocal("tmp_xforce");
            tmp_yforce = PCJ.getLocal("tmp_yforce");
            tmp_zforce = PCJ.getLocal("tmp_zforce");

            for (i = 0; i < mdsize; i++) {
                one[i].xforce = tmp_xforce[i];
                one[i].yforce = tmp_yforce[i];
                one[i].zforce = tmp_zforce[i];
            }

            // end of allreduce

            sum = 0.0;

            for (i = 0; i < mdsize; i++) {
                sum = sum + one[i].mkekin(hsq2);    /*scale forces, update velocities */

            }

            ekin = sum / hsq;

            vel = 0.0;
            count = 0.0;

            for (i = 0; i < mdsize; i++) {
                vel = vel + one[i].velavg(vaverh, h); /* average velocity */

            }

            vel = vel / h;

            /* tmeperature scale if required */
            if ((move < istop) && (((move + 1) % irep) == 0)) {
                sc = Math.sqrt(tref / (tscale * ekin));
                for (i = 0; i < mdsize; i++) {
                    one[i].dscal(sc, 1);
                }
                ekin = tref / tscale;
            }

            /* sum to getLocal full potential energy and virial */
            if (((move + 1) % iprint) == 0) {
                ek = 24.0 * ekin;
                epot = 4.0 * epot;
                etot = ek + epot;
                temp = tscale * ekin;
                pres = den * 16.0 * (ekin - vir) / mdsize;
                vel = vel / mdsize;
                rp = (count / mdsize) * 100.0;
            }
        }
    }
}


class random {

    public int iseed;
    public double v1, v2;

    public random(int iseed, double v1, double v2) {
        this.iseed = iseed;
        this.v1 = v1;
        this.v2 = v2;
    }

    public double update() {
        double rand;
        double scale = 4.656612875e-10;

        int is1, is2, iss2;
        int imult = 16807;
        int imod = 2147483647;

        if (iseed <= 0) {
            iseed = 1;
        }

        is2 = iseed % 32768;
        is1 = (iseed - is2) / 32768;
        iss2 = is2 * imult;
        is2 = iss2 % 32768;
        is1 = (is1 * imult + (iss2 - is2) / 32768) % (65536);

        iseed = (is1 * 32768 + is2) % imod;

        rand = scale * iseed;

        return rand;
    }

    public double seed() {
        double s, u1, u2, r;
        s = 1.0;
        do {
            u1 = update();
            u2 = update();

            v1 = 2.0 * u1 - 1.0;
            v2 = 2.0 * u2 - 1.0;
            s = v1 * v1 + v2 * v2;

        } while (s >= 1.0);

        r = Math.sqrt(-2.0 * Math.log(s) / s);

        return r;
    }
}
