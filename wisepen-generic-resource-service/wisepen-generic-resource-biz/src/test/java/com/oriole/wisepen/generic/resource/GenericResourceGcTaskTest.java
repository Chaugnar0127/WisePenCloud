package com.oriole.wisepen.generic.resource;

import com.oriole.wisepen.generic.resource.api.domain.base.GenericResourceStatus;
import com.oriole.wisepen.generic.resource.api.enums.GenericResourceStatusEnum;
import com.oriole.wisepen.generic.resource.config.GenericResourceProperties;
import com.oriole.wisepen.generic.resource.domain.entity.GenericResourceInfoEntity;
import com.oriole.wisepen.generic.resource.repository.GenericResourceInfoRepository;
import com.oriole.wisepen.generic.resource.service.IGenericResourceService;
import com.oriole.wisepen.generic.resource.task.GenericResourceGcTask;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class GenericResourceGcTaskTest {

    @Test
    void appliesNestedUploadTimeoutPolicyWhenDecidingWhetherToRefreshUploads() {
        GenericResourceProperties properties = new Binder(new MapConfigurationPropertySource(Map.of(
                "wisepen.generic-resource.upload-timeout.base-timeout-ms", "1200000",
                "wisepen.generic-resource.upload-timeout.assumed-speed-bps", "102400",
                "wisepen.generic-resource.upload-timeout.max-timeout-ms", "3600000"
        ))).bind("wisepen.generic-resource", Bindable.of(GenericResourceProperties.class)).get();
        GenericResourceInfoRepository repository = mock(GenericResourceInfoRepository.class);
        IGenericResourceService service = mock(IGenericResourceService.class);
        GenericResourceInfoEntity upload = GenericResourceInfoEntity.builder()
                .genericResourceId("upload-1")
                .uploaderId(1L)
                .size(1024L)
                .status(GenericResourceStatusEnum.UPLOADING)
                .createTime(LocalDateTime.now().minusMinutes(15))
                .build();
        when(repository.findByStatus(GenericResourceStatusEnum.UPLOADING)).thenReturn(List.of(upload));
        GenericResourceGcTask task = new GenericResourceGcTask(repository, service, properties);

        task.detectStaleUploads();
        verifyNoInteractions(service);

        upload.setCreateTime(LocalDateTime.now().minusMinutes(25));
        when(service.refreshGenericResourceStatus("upload-1", 1L))
                .thenReturn(new GenericResourceStatus(GenericResourceStatusEnum.UPLOADING));
        task.detectStaleUploads();
        verify(service).refreshGenericResourceStatus("upload-1", 1L);
        verify(service).updateStatus(eq("upload-1"),
                argThat(status -> status.getStatus() == GenericResourceStatusEnum.TRANSFER_TIMEOUT));
    }
}
