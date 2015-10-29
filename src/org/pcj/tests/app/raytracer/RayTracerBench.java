/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pcj.tests.app.raytracer;

import org.pcj.PCJ;

/**
 *
 * @author faramir
 */
public class RayTracerBench {

    Scene scene;
    /**
     * Lights for the rendering scene
     */
    Light lights[];
    /**
     * Objects (spheres) for the rendering scene
     */
    Primitive prim[];
    /**
     * The view for the rendering scene
     */
    View view;
    /**
     * Temporary ray
     */
    Ray tRay = new Ray();
    /**
     * Alpha channel
     */
    static final int alpha = 255 << 24;
    /**
     * Null vector (for speedup, instead of <code>new Vec(0,0,0)</code>
     */
    static final Vec voidVec = new Vec();
    /**
     * Temporary vect
     */
    Vec L = new Vec();
    /**
     * Current intersection instance (only one is needed!)
     */
    Isect inter = new Isect();
    /**
     * Height of the <code>Image</code> to be rendered
     */
    int height;
    /**
     * Width of the <code>Image</code> to be rendered
     */
    int width;
    int datasizes[] = {150, 500, 1000, 2500};
    long checksum = 0;

    /*
     * create a temporary checksum array for MPI
     */
    double[] tmp_checksum = new double[1];
    int size;
    int numobjects;

    /**
     * Create and initialize the scene for the rendering picture.
     *
     * @return The scene just created
     */
    Scene createScene() {
        int x = 0;
        int y = 0;

        Scene scene = new Scene();


        /*
         * create spheres
         */
        Primitive p;
        int nx = 4;
        int ny = 4;
        int nz = 4;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                for (int k = 0; k < nz; k++) {
                    double xx = 20.0 / (nx - 1) * i - 10.0;
                    double yy = 20.0 / (ny - 1) * j - 10.0;
                    double zz = 20.0 / (nz - 1) * k - 10.0;

                    p = new Sphere(new Vec(xx, yy, zz), 3);
//                    p.setColor(i/(double) (nx-1), j/(double)(ny-1), k/(double) (nz-1));
                    p.setColor(0, 0, (i + j) / (double) (nx + ny - 2));
                    p.surf.shine = 15.0;
                    p.surf.ks = 1.5 - 1.0;
                    p.surf.kt = 1.5 - 1.0;
                    scene.addObject(p);
                }
            }
        }

        /*
         * Creates five lights for the scene
         */
        scene.addLight(new Light(100, 100, -50, 1.0));
        scene.addLight(new Light(-100, 100, -50, 1.0));
        scene.addLight(new Light(100, -100, -50, 1.0));
        scene.addLight(new Light(-100, -100, -50, 1.0));
        scene.addLight(new Light(200, 200, 0, 1.0));

        /*
         * Creates a View (viewing point) for the rendering scene
         */
        View v = new View(new Vec(x, 20, -30),
                new Vec(x, y, 0),
                new Vec(0, 1, 0),
                1.0,
                35.0 * 3.14159265 / 180.0,
                1.0);
        /*
         * v.from = new Vec(x, y, -30); v.at = new Vec(x, y, -15); v.up = new
         * Vec(0, 1, 0); v.angle = 35.0 * 3.14159265 / 180.0; v.aspect = 1.0;
         * v.dist = 1.0;
         *
         */
        scene.setView(v);

        return scene;
    }

    public void setScene(Scene scene) {
        // Get the objects count
        int nLights = scene.getLights();
        int nObjects = scene.getObjects();

        lights = new Light[nLights];
        prim = new Primitive[nObjects];

        // Get the lights
        for (int l = 0; l < nLights; l++) {
            lights[l] = scene.getLight(l);
        }

        // Get the primitives
        for (int o = 0; o < nObjects; o++) {
            prim[o] = scene.getObject(o);
        }

        // Set the view
        view = scene.getView();
    }

    public void render(Interval interval) {

        // Screen variables
        int row[] = null;

        if (PCJ.myId() == 0) {
            row = new int[interval.width * (interval.yto - interval.yfrom)];
        }
        int p_row[] = new int[((((interval.width * (interval.yto - interval.yfrom))
                / interval.width) + PCJ.threadCount() - 1)
                / PCJ.threadCount()) * interval.width];

        int pixCounter = 0; //iterator
        int t_count; // temporary counter

        // Rendering variables
        int x, y, red, green, blue;
        double xlen, ylen;
        Vec viewVec;

        viewVec = Vec.sub(view.at, view.from);

        viewVec.normalize();

        Vec tmpVec = new Vec(viewVec);
        tmpVec.scale(Vec.dot(view.up, viewVec));

        Vec upVec = Vec.sub(view.up, tmpVec);
        upVec.normalize();

        Vec leftVec = Vec.cross(view.up, viewVec);
        leftVec.normalize();

        double frustrumwidth = view.dist * Math.tan(view.angle);

        upVec.scale(-frustrumwidth);
        leftVec.scale(view.aspect * frustrumwidth);

        Ray r = new Ray(view.from, voidVec);
        Vec col = new Vec();

        // Header for .ppm file
        // PCJ.log("P3");
        // PCJ.log(width + " " + height);
        // PCJ.log("255");
        // All loops are reversed for 'speedup' (cf. thinking in java p331)
        // For each line
        for (y = interval.yfrom + PCJ.myId(); y < interval.yto; y += PCJ.threadCount()) {
            ylen = (double) (2.0 * y) / (double) interval.width - 1.0;
            // PCJ.log("Doing line " + y);
            // For each pixel of the line
            for (x = 0; x < interval.width; x++) {
                xlen = (double) (2.0 * x) / (double) interval.width - 1.0;
                r.D = Vec.comb(xlen, leftVec, ylen, upVec);
                r.D.add(viewVec);
                r.D.normalize();
                col = trace(0, 1.0, r);

                // computes the color of the ray
                red = (int) (col.x * 255.0);
                if (red > 255) {
                    red = 255;
                }
                green = (int) (col.y * 255.0);
                if (green > 255) {
                    green = 255;
                }
                blue = (int) (col.z * 255.0);
                if (blue > 255) {
                    blue = 255;
                }

                checksum += red;
                checksum += green;
                checksum += blue;

                // RGB values for .ppm file
                // PCJ.log(red + " " + green + " " + blue);
                // Sets the pixels
                p_row[pixCounter++] = alpha | (red << 16) | (green << 8) | (blue);
            } // end for (x)
        } // end for (y)

        /*
         * carry out a global sum on checksum
         */
//        tmp_checksum[0] = (double) checksum;
//        MPI.COMM_WORLD.Reduce(tmp_checksum, 0, tmp_checksum, 0, 1, MPI.DOUBLE, MPI.SUM, 0);
//        if (PCJ.myId() == 0) {
//            checksum = (long) tmp_checksum[0];
//        }
        PCJ.put(0, "r_checksum", checksum, PCJ.myId());
        PCJ.put(0, "r_p_row", p_row, PCJ.myId());
        if (PCJ.myId() == 0) {
            PCJ.waitFor("r_checksum", PCJ.threadCount());

            checksum = 0;
            long[] r_checksum = PCJ.getLocal("r_checksum");
            for (long tmp : r_checksum) {
                checksum += tmp;
            }
            /*
             * send temporary copies of p_row back to row
             */
            PCJ.waitFor("r_p_row", PCJ.threadCount());
            int[][] r_p_row = PCJ.getLocal("r_p_row");

            for (int k = 0; k < PCJ.threadCount(); k++) {
                p_row = r_p_row[k];

                t_count = 0;
                for (int i = k; i < (interval.yto - interval.yfrom); i += PCJ.threadCount()) {
                    for (x = 0; x < interval.width; x++) {
                        row[i * interval.width + x] = p_row[t_count];
                        t_count++;
                    }
                }
            }
        }
    }

    boolean intersect(Ray r, double maxt) {
        Isect tp;
        int i, nhits;

        nhits = 0;
        inter.t = 1e9;
        for (i = 0; i < prim.length; i++) {
            // uses global temporary Prim (tp) as temp.object for speedup
            tp = prim[i].intersect(r);
            if (tp != null && tp.t < inter.t) {
                inter.t = tp.t;
                inter.prim = tp.prim;
                inter.surf = tp.surf;
                inter.enter = tp.enter;
                nhits++;
            }
        }
        return nhits > 0 ? true : false;
    }

    /**
     * Checks if there is a shadow
     *
     * @param r The ray
     * @return Returns 1 if there is a shadow, 0 if there isn't
     */
    int Shadow(Ray r, double tmax) {
        if (intersect(r, tmax)) {
            return 0;
        }
        return 1;
    }

    /**
     * Return the Vector's reflection direction
     *
     * @return The specular direction
     */
    Vec SpecularDirection(Vec I, Vec N) {
        Vec r;
        r = Vec.comb(1.0 / Math.abs(Vec.dot(I, N)), I, 2.0, N);
        r.normalize();
        return r;
    }

    /**
     * Return the Vector's transmission direction
     */
    Vec TransDir(Surface m1, Surface m2, Vec I, Vec N) {
        double n1, n2, eta, c1, cs2;
        Vec r;
        n1 = m1 == null ? 1.0 : m1.ior;
        n2 = m2 == null ? 1.0 : m2.ior;
        eta = n1 / n2;
        c1 = -Vec.dot(I, N);
        cs2 = 1.0 - eta * eta * (1.0 - c1 * c1);
        if (cs2 < 0.0) {
            return null;
        }
        r = Vec.comb(eta, I, eta * c1 - Math.sqrt(cs2), N);
        r.normalize();
        return r;
    }

    /**
     * Returns the shaded color
     *
     * @return The color in Vec form (rgb)
     */
    Vec shade(int level, double weight, Vec P, Vec N, Vec I, Isect hit) {
        double n1, n2, eta, c1, cs2;
        Vec r;
        Vec tcol;
        Vec R;
        double t, diff, spec;
        Surface surf;
        Vec col;
        int l;

        col = new Vec();
        surf = hit.surf;
        R = new Vec();
        if (surf.shine > 1e-6) {
            R = SpecularDirection(I, N);
        }

        // Computes the effectof each light
        for (l = 0; l < lights.length; l++) {
            L.sub2(lights[l].pos, P);
            if (Vec.dot(N, L) >= 0.0) {
                t = L.normalize();

                tRay.P = P;
                tRay.D = L;

                // Checks if there is a shadow
                if (Shadow(tRay, t) > 0) {
                    diff = Vec.dot(N, L) * surf.kd
                            * lights[l].brightness;

                    col.adds(diff, surf.color);
                    if (surf.shine > 1e-6) {
                        spec = Vec.dot(R, L);
                        if (spec > 1e-6) {
                            spec = Math.pow(spec, surf.shine);
                            col.x += spec;
                            col.y += spec;
                            col.z += spec;
                        }
                    }
                }
            } // if
        } // for

        tRay.P = P;
        if (surf.ks * weight > 1e-3) {
            tRay.D = SpecularDirection(I, N);
            tcol = trace(level + 1, surf.ks * weight, tRay);
            col.adds(surf.ks, tcol);
        }
        if (surf.kt * weight > 1e-3) {
            if (hit.enter > 0) {
                tRay.D = TransDir(null, surf, I, N);
            } else {
                tRay.D = TransDir(surf, null, I, N);
            }
            tcol = trace(level + 1, surf.kt * weight, tRay);
            col.adds(surf.kt, tcol);
        }

        // garbaging...
        tcol = null;
        surf = null;

        return col;
    }

    /**
     * Launches a ray
     */
    Vec trace(int level, double weight, Ray r) {
        Vec P, N;
        boolean hit;

        // Checks the recursion level
        if (level > 6) {
            return new Vec();
        }

        hit = intersect(r, 1e6);
        if (hit) {
            P = r.point(inter.t);
            N = inter.prim.normal(P);
            if (Vec.dot(r.D, N) >= 0.0) {
                N.negate();
            }
            return shade(level, weight, P, N, r.D, inter);
        }
        // no intersection --> col = 0,0,0
        return voidVec;
    }

    public void JGFinitialise() {

        // set image size
        width = height = datasizes[size];
        if (PCJ.myId() == 0) {
            PCJ.putLocal("r_checksum", new long[PCJ.threadCount()]);
            PCJ.putLocal("r_p_row", new int[PCJ.threadCount()][0]);

            PCJ.monitor("r_checksum");
            PCJ.monitor("r_p_row");
        }

        // create the objects to be rendered
        scene = createScene();

        // getLocal lights, objects etc. from scene.
        setScene(scene);

        numobjects = scene.getObjects();
    }

    public void JGFapplication() {
        // Set interval to be rendered to the whole picture
        // (overkill, but will be useful to retain this for parallel versions)
        Interval interval = new Interval(0, width, height, 0, height, 1);

        // Do the business!
        render(interval);
    }

    public void JGFvalidate() {
        long refval[] = {2676692, 29827635, 119283198, 745422489L};
        long dev = checksum - refval[size];
        if (dev != 0) {
            System.err.println("Validation failed");
            System.err.println("Pixel checksum = " + checksum);
            System.err.println("Reference value = " + refval[size]);
        }
    }

    public void run(int size) {
        this.size = size;

        JGFinitialise();

        PCJ.barrier();
        long time = System.nanoTime();
        JGFapplication();
        PCJ.barrier();

        time = System.nanoTime() - time;
        double dtime = time * 1e-9;

        if (PCJ.myId() == 0) {
            JGFvalidate();

            System.out.format("RayTracer%d\t%5d\ttime %12.7f%n",
                    size, PCJ.threadCount(), dtime);
        }
    }
}
