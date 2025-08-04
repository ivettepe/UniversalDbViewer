package com.db.viewer.dbviewer.controller;

import com.db.viewer.dbviewer.service.DynamicTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/viewer")
public class TableViewerController {

    private final DynamicTableService service;

    @GetMapping
    public String viewTables(Model model) {
        List<String> tableNames = service.getAllTableNames();
        model.addAttribute("tables", tableNames);
        return "tables";
    }

    @GetMapping("/table")
    public String viewTable(
            @RequestParam String table,
            @RequestParam(required = false) List<String> columns,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model
    ) {
        if (columns == null) columns = new ArrayList<>();

        var data = service.getTableData(table, columns, page, size);
        var headers = columns.isEmpty() ? service.getTableColumns(table) : columns;
        var totalRows = service.countRows(table);
        var totalPages = (int) Math.ceil((double) totalRows / size);

        System.out.println("data: " + data);
        System.out.println("headers: " + headers);
        System.out.println("totalRows: " + totalRows);
        System.out.println("totalPages: " + totalPages);

        model.addAttribute("headers", headers);
        model.addAttribute("rows", data);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("table", table);
        model.addAttribute("columns", columns);
        model.addAttribute("size", size);

        return "table";
    }

    @GetMapping("/edit")
    public String editRecord(@RequestParam String table,
                             @RequestParam(required = false) List<String> columns,
                             @RequestParam Long id,
                             Model model) {
        Map<String, Object> row = service.getRowById(table, id);
        if (columns == null) {
            columns = service.getTableColumns(table, id);
        }
        model.addAttribute("table", table);
        model.addAttribute("id", id);
        model.addAttribute("columns", columns);
        model.addAttribute("row", row);
        return "edit";
    }

    @GetMapping("/create")
    public String createForm(@RequestParam String table,
                             @RequestParam(required = false) List<String> columns,
                             Model model) {
        if (columns == null) {
            columns = service.getTableColumns(table);
        }
        model.addAttribute("table", table);
        model.addAttribute("columns", columns);
        model.addAttribute("id", null);
        model.addAttribute("row", new HashMap<String, Object>());
        return "edit";
    }

    @PostMapping("/save")
    public String saveRecord(@RequestParam String table,
                             @RequestParam(required = false) Long id,
                             @RequestParam Map<String, Object> params) {
        params.remove("table");
        params.remove("id");
        if (id == null) {
            service.insertRow(table, params);
        } else {
            service.updateRow(table, id, params);
        }
        return "redirect:/viewer/table?table=" + table;
    }

    @GetMapping("/delete")
    public String deleteRecord(@RequestParam String table,
                               @RequestParam Long id) {
        service.deleteRow(table, id);
        return "redirect:/viewer/table?table=" + table;
    }

}
