package org.demo.webflux.dto.list;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.demo.webflux.dto.AuthorDto;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthorListDto {

    private List<AuthorDto> authors;

}
