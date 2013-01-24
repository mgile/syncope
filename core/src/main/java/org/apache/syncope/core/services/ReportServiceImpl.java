/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.core.services;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.apache.syncope.common.services.ReportService;
import org.apache.syncope.common.to.ReportExecTO;
import org.apache.syncope.common.to.ReportTO;
import org.apache.syncope.common.types.ReportExecExportFormat;
import org.apache.syncope.core.persistence.beans.ReportExec;
import org.apache.syncope.core.persistence.dao.ReportDAO;
import org.apache.syncope.core.rest.controller.ReportController;
import org.apache.syncope.core.util.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceImpl implements ReportService, ContextAware {
    @Autowired
    ReportController reportController;
    
    @Autowired
    private ReportDAO reportDAO;
    
    private UriInfo uriInfo;

    @Override
    public Response create(ReportTO reportTO) {
        ReportTO createdReportTO = reportController.createInternal(reportTO);
        URI location = uriInfo.getAbsolutePathBuilder().path("" + createdReportTO.getId()).build();
        return Response.created(location).build();
    }

    @Override
    public void update(@PathParam("reportId") Long reportId, ReportTO reportTO) {
        try {
            reportController.update(reportTO);
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public int count() {
        return reportDAO.count();
    }

    @Override
    public List<ReportTO> list() {
        return reportController.list();
    }

    @Override
    public List<ReportTO> list(@QueryParam("page") int page,
            @QueryParam("size") @DefaultValue("25") int size) {
        return reportController.list(page, size);
    }

    @Override
    public List<ReportExecTO> listExecutions() {
        return reportController.listExecutions();
    }

    @Override
    public Set<String> getReportletConfClasses() {
        return reportController.getReportletConfClassesInternal();
    }

    @Override
    public ReportTO read(@PathParam("reportId") Long reportId) {
        try {
            return reportController.read(reportId);
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException();
        }
    }

    @Override
    public ReportExecTO readExecution(@PathParam("executionId") Long executionId) {
        try {
            return reportController.readExecution(executionId);
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public Response exportExecutionResult(final @PathParam("executionId") Long executionId,
            final @QueryParam("format") ReportExecExportFormat fmt) {
        final ReportExecExportFormat format = (fmt == null) ? ReportExecExportFormat.XML : fmt;
        try {
            final ReportExec reportExec = reportController.getAndCheckReportExecInternal(executionId);
            return Response.ok(new StreamingOutput() {
                public void write(final OutputStream os) throws IOException {
                    reportController.exportExecutionResultInternal(os, reportExec, format);
                }
            }).build();
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public ReportExecTO execute(@PathParam("reportId") Long reportId) {
        try {
            return reportController.execute(reportId);
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public void delete(@PathParam("reportId") Long reportId) {
        try {
            reportController.delete(reportId);
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public void deleteExecution(@PathParam("executionId") Long executionId) {
        try {
            reportController.deleteExecution(executionId);
        } catch (NotFoundException e) {
            throw new javax.ws.rs.NotFoundException(e);
        }
    }

    @Override
    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

}
