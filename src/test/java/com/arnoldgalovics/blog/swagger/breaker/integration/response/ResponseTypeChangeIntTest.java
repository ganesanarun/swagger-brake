package com.arnoldgalovics.blog.swagger.breaker.integration.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.arnoldgalovics.blog.swagger.breaker.core.BreakingChange;
import com.arnoldgalovics.blog.swagger.breaker.core.model.HttpMethod;
import com.arnoldgalovics.blog.swagger.breaker.core.rule.response.ResponseTypeAttributeRemovedBreakingChange;
import com.arnoldgalovics.blog.swagger.breaker.integration.AbstractSwaggerBreakerTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class ResponseTypeChangeIntTest extends AbstractSwaggerBreakerTest {
    @Test
    public void testResponseTypeChangeIsBreakingChangeWhenExistingAttributeRemoved() {
        // given
        String oldApiPath = "response/attributeremoved/petstore.yaml";
        String newApiPath = "response/attributeremoved/petstore_v2.yaml";
        ResponseTypeAttributeRemovedBreakingChange bc1 = new ResponseTypeAttributeRemovedBreakingChange("/pet/findByStatus", HttpMethod.GET, "200", "category");
        ResponseTypeAttributeRemovedBreakingChange bc3 = new ResponseTypeAttributeRemovedBreakingChange("/pet/{petId}", HttpMethod.GET, "200", "category");
        ResponseTypeAttributeRemovedBreakingChange bc2 = new ResponseTypeAttributeRemovedBreakingChange("/pet/findByTags", HttpMethod.GET, "200", "category");
        Collection<BreakingChange> expected = Arrays.asList(bc1, bc2, bc3);
        // when
        Collection<BreakingChange> result = underTest.execute(oldApiPath, newApiPath);
        // then
        assertThat(result).hasSize(3);
        assertThat(result).hasSameElementsAs(expected);
    }

    @Test
    public void testResponseTypeChangeIsNotBreakingChangeWhenDifferentTypeIsUsedButSameAttributes() {
        // given
        String oldApiPath = "response/differenttypesameattributes/petstore.yaml";
        String newApiPath = "response/differenttypesameattributes/petstore_v2.yaml";
        // when
        Collection<BreakingChange> result = underTest.execute(oldApiPath, newApiPath);
        // then
        assertThat(result).hasSize(0);
    }

    @Test
    public void testResponseTypeChangeIsBreakingChangeWhenDifferentTypeIsUsedWithDifferentAttributes() {
        // given
        String oldApiPath = "response/differenttypesdifferentattributes/petstore.yaml";
        String newApiPath = "response/differenttypesdifferentattributes/petstore_v2.yaml";
        ResponseTypeAttributeRemovedBreakingChange bc = new ResponseTypeAttributeRemovedBreakingChange("/pet/findByStatus", HttpMethod.GET, "200", "category");
        Collection<BreakingChange> expected = Collections.singleton(bc);
        // when
        Collection<BreakingChange> result = underTest.execute(oldApiPath, newApiPath);
        // then
        assertThat(result).hasSize(1);
        assertThat(result).hasSameElementsAs(expected);
    }
}