/*******************************************************************************
 * Copyright (c) 2004, 2005 Actuate Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Actuate Corporation  - initial API and implementation
 *******************************************************************************/

package org.eclipse.birt.data.engine.executor.transform.pass;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.birt.data.engine.api.querydefn.ComputedColumn;
import org.eclipse.birt.data.engine.core.DataException;
import org.eclipse.birt.data.engine.executor.transform.ResultSetPopulator;
import org.eclipse.birt.data.engine.executor.transform.TransformationConstants;
import org.eclipse.birt.data.engine.impl.ComputedColumnHelper;
import org.eclipse.birt.data.engine.impl.FilterByRow;

/**
 * The abstract class defines the common behavior of DataSetProcessUtil and 
 * ResultSetProcessUtil.  
 *
 */
abstract class RowProcessUtil
{
	/**
	 * 
	 */
	protected ComputedColumnsState iccState;
	protected ComputedColumnHelper computedColumnHelper;
	protected FilterByRow filterByRow;
	protected PassStatusController psController;
	protected ResultSetPopulator populator;
	
	/**
	 * 
	 * @param populator
	 * @param iccState
	 * @param computedColumnHelper
	 * @param filterByRow
	 * @param psController
	 */
	protected RowProcessUtil( ResultSetPopulator populator,
			ComputedColumnsState iccState,
			ComputedColumnHelper computedColumnHelper,
			FilterByRow filterByRow,
			PassStatusController psController)
	{
		this.iccState = iccState;
		this.computedColumnHelper = computedColumnHelper;
		this.filterByRow = filterByRow;
		this.psController = psController;
		this.populator = populator;
	}
	
	/**
	 * 
	 * @param model
	 * @return
	 * @throws DataException
	 */
	protected List prepareComputedColumns( int model )
			throws DataException
	{
		initializeICCState( model );
		
		List aggCCList = new ArrayList( );
		List simpleCCList = new ArrayList( );
		if ( computedColumnHelper != null )
		{
			computedColumnHelper.setModel( model );
			List l = computedColumnHelper.getComputedColumnList( );
			for ( int i = 0; i < l.size( ); i++ )
			{
				if ( this.populator.getExpressionProcessor( )
						.hasAggregation( ( (ComputedColumn) l.get( i ) ).getExpression( ) ) )
				{
					aggCCList.add( l.get( i ) );
				}
				else
				{
					simpleCCList.add( l.get( i ) );
				}
			}
			computedColumnHelper.getComputedColumnList( ).clear( );
			computedColumnHelper.getComputedColumnList( ).addAll( simpleCCList );
			computedColumnHelper.setRePrepare( true );
		}

		return aggCCList;
	}

	/**
	 * 
	 * @param model
	 * @throws DataException
	 */
	private void initializeICCState( int model ) throws DataException
	{
		if ( iccState != null )
		{
			iccState.setModel( model );

			for ( int i = 0; i < iccState.getCount( ); i++ )
			{
				if ( !this.populator.getExpressionProcessor( )
						.hasAggregation( iccState.getComputedColumn( i )
								.getExpression( ) ) )
				{
					iccState.setValueAvailable( i );
				}

			}

			if ( model == TransformationConstants.DATA_SET_MODEL )
			{
				this.populator.getExpressionProcessor( )
						.prepareComputedColumns( iccState );
			}
			else if ( model == TransformationConstants.RESULT_SET_MODEL )
			{
	/*			iccState.setModel( TransformationConstants.DATA_SET_MODEL );
				this.populator.getExpressionProcessor( )
						.prepareComputedColumns( iccState );
				iccState.setModel( TransformationConstants.RESULT_SET_MODEL );
	*/			this.populator.getExpressionProcessor( )
						.prepareComputedColumns( iccState );
			}
		}
	}
	

	/**
	 * 
	 * @param filterType
	 * @param changeMaxRows
	 * @throws DataException
	 */
	protected void applyFilters( int filterType, boolean changeMaxRows )
			throws DataException
	{
		if ( filterByRow != null && filterByRow.isFilterSetExist( filterType ) )
		{
			int max = populator.getQuery( ).getMaxRows( );

			if ( changeMaxRows )
			{
				populator.getQuery( ).setMaxRows( 0 );
			}
			filterByRow.setWorkingFilterSet( filterType );
			FilterCalculator.applyFilters( this.populator,
					this.filterByRow );
			populator.getQuery( ).setMaxRows( max );
		}

		if ( filterByRow != null )
		{
			filterByRow.setWorkingFilterSet( FilterByRow.NO_FILTER );
		}
	}
}
