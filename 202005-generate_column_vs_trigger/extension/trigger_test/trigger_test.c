#include <stdio.h>
#include <math.h>
#include <float.h>
#include "postgres.h"
#include "utils/rel.h"
#include "executor/spi.h"
#include "commands/trigger.h"
//#include "utils/fmgrprotos.h"
#ifdef PG_MODULE_MAGIC
PG_MODULE_MAGIC;
#endif
extern Datum trigger_test(PG_FUNCTION_ARGS);
PG_FUNCTION_INFO_V1(trigger_test);
Datum
trigger_test(PG_FUNCTION_ARGS)
{
    TriggerData *trigdata = (TriggerData *) fcinfo->context;
    TupleDesc   tdesc;
	HeapTuple   tuple;
    HeapTuple   rettuple;
    int         attnum;
    int         r;
    Datum       datumValar;
	bool        isnull;
    //Get tuple description
    tdesc = trigdata->tg_relation->rd_att;
    tuple = NULL;
	
	//Make sure that the function is called from a trigger
    if (!CALLED_AS_TRIGGER(fcinfo))
        elog(ERROR, "are you sure you are calling from trigger manager?");
    //If the trigger is part of an UPDATE event
    if (TRIGGER_FIRED_BY_UPDATE(trigdata->tg_event))
    {
        attnum = 2;
        tuple = trigdata->tg_newtuple;
		//get value of column
		r = DatumGetInt32(SPI_getbinval(tuple, tdesc, 1, &isnull));
		//Calculating area value
	    //datumValar = Int32GetDatum(r+r); 
        datumValar = Float8GetDatum( pow(r,2) );
    
    }
    //If the trigger is part of an INSERT event
    if (TRIGGER_FIRED_BY_INSERT(trigdata->tg_event))
    {
        
		attnum = 2;
        tuple = trigdata->tg_trigtuple; 
		//get value of column
	    r  = DatumGetInt32(SPI_getbinval(tuple, tdesc, 1, &isnull));
	    //Calculating area value
        //datumValar = Int32GetDatum(r+r); 
        datumValar = Float8GetDatum( pow(r,2) );

    }
    //Connect to Server and modify the tuple
    SPI_connect();
    rettuple = SPI_modifytuple(trigdata->tg_relation, tuple, 1, &attnum, &datumValar, NULL);
    if (rettuple == NULL)
    {
        if (SPI_result == SPI_ERROR_ARGUMENT || SPI_result == SPI_ERROR_NOATTRIBUTE)
                elog(ERROR, "SPI_result failed! SPI_ERROR_ARGUMENT or SPI_ERROR_NOATTRIBUTE in trigger_test!");
         elog(ERROR, "SPI_modifytuple failed in trigger_test!");
    }
    SPI_finish();   
    return PointerGetDatum(rettuple);
}