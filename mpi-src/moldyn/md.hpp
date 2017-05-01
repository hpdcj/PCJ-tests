#ifndef TMD
#define TMD

struct Random;
struct particle;

const int ITERS = 100;
const double LENGTH = 50e-10;
const double m = 4.0026;
const double mu = 1.66056e-27;
const double kb = 1.38066e-23;
const double TSIM = 50;
const double deltat = 5e-16;
particle** one;
double epot = 0.0;
double vir = 0.0;
double count = 0.0;
int datasizes[8] = {8,13, 16, 24, 32, 40, 48, 56};

int interactions = 0;

int i,j,k,lg,mdsize,move,mm;

double l,rcoff,rcoffs,side,sideh,hsq,hsq2,vel; 
double a,r,sum,tscale,sc,ekin,ek,ts,sp;    
double den = 0.83134;
double tref = 0.722;
double h = 0.064;
double vaver,vaverh;
double etot,temp,pres,rp;
double u1,u2,v1,v2,s;

double* tmp_xforce;
double* tmp_yforce;
double* tmp_zforce;

double* tmp_epot;
double* tmp_vir;
int* tmp_interactions;

int ijk,npartm,PARTSIZE,iseed,tint;
int irep = 10;
int istop = 19;
int iprint = 10;
int movemx = 50;

Random* randnum;
#include "particle.hpp"

void initialise() {

    /* Parameter determination */

    mm = datasizes[size];
    PARTSIZE = mm*mm*mm*4;
    mdsize = PARTSIZE;
    one = new particle* [mdsize];
    l = LENGTH;

    side = pow((mdsize/den),0.3333333);
    rcoff = mm/4.0;

    a = side/mm;
    sideh = side*0.5;
    hsq = h*h;
    hsq2 = hsq*0.5;
    npartm = mdsize - 1;
    rcoffs = rcoff * rcoff;
    tscale = 16.0 / (1.0 * mdsize - 1.0);
    vaver = 1.13 * sqrt(tref / 24.0);
    vaverh = vaver * h;

    /* temporary arrays for MPI operations */

    tmp_xforce = new double [mdsize];
    tmp_yforce = new double [mdsize];
    tmp_zforce = new double [mdsize];

    tmp_epot = new double[1];
    tmp_vir = new double[1];


    /* Particle Generation */

    ijk = 0;
    for (lg=0; lg<=1; lg++) {
        for (i=0; i<mm; i++) {
            for (j=0; j<mm; j++) {
                for (k=0; k<mm; k++) {
                    one[ijk] = new particle((i*a+lg*a*0.5),(j*a+lg*a*0.5),(k*a),
                            0.0,0.0,0.0,0.0,0.0,0.0);
                    ijk = ijk + 1;
                }
            }
        }
    }
    for (lg=1; lg<=2; lg++) {
        for (i=0; i<mm; i++) {
            for (j=0; j<mm; j++) {
                for (k=0; k<mm; k++) {
                    one[ijk] = new particle((i*a+(2-lg)*a*0.5),(j*a+(lg-1)*a*0.5),
                            (k*a+a*0.5),0.0,0.0,0.0,0.0,0.0,0.0);
                    ijk = ijk + 1;
                }
            }
        }
    }

    /* Initialise velocities */

    iseed = 0;
    v1 = 0.0;
    v2 = 0.0;

    randnum = new Random(iseed,v1,v2);

    for (i=0; i<mdsize; i+=2) {
        r = randnum->seed();
        one[i]->xvelocity = r*randnum->v1;
        one[i+1]->xvelocity  = r*randnum->v2;
    }

    for (i=0; i<mdsize; i+=2) {
        r  = randnum->seed();
        one[i]->yvelocity = r*randnum->v1;
        one[i+1]->yvelocity  = r*randnum->v2;
    }

    for (i=0; i<mdsize; i+=2) {
        r  = randnum->seed();
        one[i]->zvelocity = r*randnum->v1;
        one[i+1]->zvelocity  = r*randnum->v2;
    }

    /* velocity scaling */

    ekin = 0.0;
    sp = 0.0;

    for(i=0;i<mdsize;i++) {
        sp = sp + one[i]->xvelocity;
    }
    sp = sp / mdsize;

    for(i=0;i<mdsize;i++) {
        one[i]->xvelocity = one[i]->xvelocity - sp;
        ekin = ekin + one[i]->xvelocity*one[i]->xvelocity;
    }

    sp = 0.0;
    for(i=0;i<mdsize;i++) {
        sp = sp + one[i]->yvelocity;
    }
    sp = sp / mdsize;

    for(i=0;i<mdsize;i++) {
        one[i]->yvelocity = one[i]->yvelocity - sp;
        ekin = ekin + one[i]->yvelocity*one[i]->yvelocity;
    }

    sp = 0.0;
    for(i=0;i<mdsize;i++) {
        sp = sp + one[i]->zvelocity;
    }
    sp = sp / mdsize;

    for(i=0;i<mdsize;i++) {
        one[i]->zvelocity = one[i]->zvelocity - sp;
        ekin = ekin + one[i]->zvelocity*one[i]->zvelocity;
    }

    ts = tscale * ekin;
    sc = h * sqrt(tref/ts);

    for(i=0;i<mdsize;i++) {

        one[i]->xvelocity = one[i]->xvelocity * sc;     
        one[i]->yvelocity = one[i]->yvelocity * sc;     
        one[i]->zvelocity = one[i]->zvelocity * sc;     

    }

    /* MD simulation */

}

void runiters() {

    move = 0;
    for (move=0;move<movemx;move++) {

        for (i=0;i<mdsize;i++) {
            one[i]->domove(side);        /* move the particles and update velocities */
        }

        epot = 0.0;
        vir = 0.0;

        MPI_Barrier(MPI_COMM_WORLD);

        for (i=0+rank;i<mdsize;i+=nprocess) {
            one[i]->force(side,rcoff,mdsize,i);  /* compute forces */
        }

        MPI_Barrier(MPI_COMM_WORLD);

        /* global reduction on partial sums of the forces, epot, vir and interactions */ 


        for (i=0;i<mdsize;i++) {
            tmp_xforce[i] = one[i]->xforce; 
            tmp_yforce[i] = one[i]->yforce; 
            tmp_zforce[i] = one[i]->zforce; 
        }
double* tmp = new double[mdsize];
        MPI_Allreduce(tmp_xforce,tmp,mdsize,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
for (i=0;i<mdsize;++i) tmp_xforce[i]=tmp[i];
        MPI_Allreduce(tmp_yforce,tmp,mdsize,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
for (i=0;i<mdsize;++i) tmp_yforce[i]=tmp[i];
        MPI_Allreduce(tmp_zforce,tmp,mdsize,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
for (i=0;i<mdsize;++i) tmp_zforce[i]=tmp[i];
delete tmp;

        for (i=0;i<mdsize;i++) {
            one[i]->xforce = tmp_xforce[i]; 
            one[i]->yforce = tmp_yforce[i];
            one[i]->zforce = tmp_zforce[i];
        }

        tmp_epot[0] = epot; 
        tmp_vir[0] = vir; 

        MPI_Allreduce(tmp_epot,&epot,1,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);
        MPI_Allreduce(tmp_vir,&vir,1,MPI_DOUBLE,MPI_SUM,MPI_COMM_WORLD);

//        epot = tmp_epot[0]; 
//        vir = tmp_vir[0]; 

        MPI_Barrier(MPI_COMM_WORLD);

        sum = 0.0;

        for (i=0;i<mdsize;i++) {
            sum = sum + one[i]->mkekin(hsq2);    /*scale forces, update velocities */
        }

        ekin = sum/hsq;

        vel = 0.0;
        count = 0.0;

        for (i=0;i<mdsize;i++) {
            vel = vel + one[i]->velavg(vaverh,h); /* average velocity */
        }

        vel = vel / h;

        /* tmeperature scale if required */

        if((move < istop) && (((move+1) % irep) == 0)) {
            sc = sqrt(tref / (tscale*ekin));
            for (i=0;i<mdsize;i++) {
                one[i]->dscal(sc,1);
            }
            ekin = tref / tscale;
        }

        /* sum to get full potential energy and virial */

        if(((move+1) % iprint) == 0) {
            ek = 24.0*ekin;
            epot = 4.0*epot;
            etot = ek + epot;
            temp = tscale * ekin;
            pres = den * 16.0 * (ekin - vir) / mdsize;
            vel = vel / mdsize; 
            rp = (count / mdsize) * 100.0;
        }

    }

	tmp_interactions = new int[1];
        tmp_interactions[0] = interactions; 
        MPI_Reduce(tmp_interactions,&interactions,1,MPI_INT,MPI_SUM,0,MPI_COMM_WORLD);
//        interactions = tmp_interactions[0]; 


}
#endif
