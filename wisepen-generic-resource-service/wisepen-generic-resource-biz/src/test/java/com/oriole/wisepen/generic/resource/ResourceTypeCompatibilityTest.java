package com.oriole.wisepen.generic.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oriole.wisepen.common.core.exception.ServiceException;
import com.oriole.wisepen.file.storage.api.enums.StorageSceneEnum;
import com.oriole.wisepen.file.storage.api.feign.RemoteStorageService;
import com.oriole.wisepen.generic.resource.api.domain.dto.req.GenericResourceUploadInitRequest;
import com.oriole.wisepen.generic.resource.mq.GenericResourceEventPublisher;
import com.oriole.wisepen.generic.resource.repository.GenericResourceInfoRepository;
import com.oriole.wisepen.generic.resource.service.impl.GenericResourceServiceImpl;
import com.oriole.wisepen.resource.enums.ResourceType;
import com.oriole.wisepen.resource.feign.RemoteResourceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResourceTypeCompatibilityTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @ParameterizedTest
    @CsvSource({
            "SINGLE_HTML_PAGE, SINGLE_HTML_PAGE", "html, SINGLE_HTML_PAGE", ".HTML, SINGLE_HTML_PAGE",
            "ARCHIVE, ARCHIVE", ".ZIP, ARCHIVE", "7z, ARCHIVE", "tgz, ARCHIVE",
            "BINARY, BINARY", ".BIN, BINARY", "dat, BINARY", ".DOCX, DOCX"
    })
    void resolvesTypeValuesAndFileExtensions(String input, ResourceType expected) {
        assertEquals(expected, ResourceType.fromExtension(" " + input + " "));
    }

    @Test
    void keepsUnknownExtensionsAvailableForGenericResourceFallback() {
        assertNull(ResourceType.fromExtension(null));
        assertNull(ResourceType.fromExtension(""));
        assertNull(ResourceType.fromExtension("custom-extension"));
        assertEquals(ResourceType.UNKNOWN, ResourceType.fromExtension("UNKNOWN"));
    }

    @Test
    void roundTripsResourceTypesIncludingDeletionMessageKeys() throws Exception {
        for (ResourceType type : ResourceType.values()) {
            assertEquals(type, objectMapper.readValue(objectMapper.writeValueAsString(type), ResourceType.class));
        }
        Map<ResourceType, List<String>> typedResourceIds = Map.of(
                ResourceType.SINGLE_HTML_PAGE, List.of("page-1"),
                ResourceType.ARCHIVE, List.of("archive-1"),
                ResourceType.BINARY, List.of("binary-1"));
        assertEquals(typedResourceIds, objectMapper.readValue(objectMapper.writeValueAsString(typedResourceIds),
                new TypeReference<Map<ResourceType, List<String>>>() {}));
    }

    @Test
    void keepsStorageScenesDistinctForCallbacksAndLegacyNumericInput() throws Exception {
        assertEquals(StorageSceneEnum.values().length,
                Arrays.stream(StorageSceneEnum.values()).map(StorageSceneEnum::getCode).distinct().count());
        assertEquals(ResourceType.values().length,
                Arrays.stream(ResourceType.values()).map(ResourceType::getCode).distinct().count());
        assertEquals(StorageSceneEnum.PRIVATE_WEB_APP_ASSET, StorageSceneEnum.fromValue("8"));
        assertEquals(StorageSceneEnum.PRIVATE_GENERIC_RESOURCE, StorageSceneEnum.fromValue("9"));
        for (StorageSceneEnum scene : StorageSceneEnum.values()) {
            assertEquals(scene, objectMapper.readValue(objectMapper.writeValueAsString(scene), StorageSceneEnum.class));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"html", ".HTML", "SINGLE_HTML_PAGE"})
    void rejectsWebAppUploadsBeforeCreatingStorageOrResourceRecords(String extension) {
        GenericResourceInfoRepository repository = mock(GenericResourceInfoRepository.class);
        RemoteStorageService storageService = mock(RemoteStorageService.class);
        RemoteResourceService resourceService = mock(RemoteResourceService.class);
        GenericResourceEventPublisher publisher = mock(GenericResourceEventPublisher.class);
        GenericResourceServiceImpl service = new GenericResourceServiceImpl(
                repository, storageService, resourceService, publisher);

        assertThrows(ServiceException.class, () -> service.initUploadGenericResource(
                GenericResourceUploadInitRequest.builder().extension(extension).build(), 1L, Map.of()));
        verifyNoInteractions(repository, storageService, resourceService, publisher);
    }
}
