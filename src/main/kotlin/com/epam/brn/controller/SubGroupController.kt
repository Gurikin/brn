package com.epam.brn.controller

import com.epam.brn.dto.response.BaseResponse
import com.epam.brn.dto.response.BaseSingleObjectResponse
import com.epam.brn.service.SubGroupService
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/subgroups")
@Api(value = "/subgroups", description = "Contains actions over subgroup")
class SubGroupController(private val subGroupsService: SubGroupService) {

    @GetMapping
    @ApiOperation("Get subgroups for series")
    fun getAllGroups(@RequestParam(value = "seriesId", required = true) seriesId: Long): ResponseEntity<BaseResponse> {
        val data = subGroupsService.findSubGroupsForSeries(seriesId)
        return ResponseEntity.ok().body(BaseResponse(data = data))
    }

    @GetMapping("{subGroupId}")
    @ApiOperation("Get subgroup for id")
    fun getSeriesForId(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BaseSingleObjectResponse> {
        val subGroupDto = subGroupsService.findById(subGroupId)
        return ResponseEntity.ok(BaseSingleObjectResponse(data = subGroupDto))
    }

    @DeleteMapping("{subGroupId}")
    @ApiOperation("Delete subgroup by id without exercises")
    fun deleteSubGroupById(@PathVariable(value = "subGroupId") subGroupId: Long): ResponseEntity<BaseSingleObjectResponse> {
        subGroupsService.deleteSubGroupById(subGroupId)
        return ResponseEntity.ok(BaseSingleObjectResponse(data = Unit))
    }
}
