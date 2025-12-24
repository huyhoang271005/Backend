package com.example.hello.Mapper;

import com.example.hello.Entity.Contact;
import com.example.hello.Feature.Contact.ContactDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ContactMapper {
    List<ContactDTO> toContactDTOList(List<Contact> contacts);
    ContactDTO toContactDto(Contact contact);
    @Mapping(target = "contactId", ignore = true)
    Contact toContact(ContactDTO contactDTO);
    void updateContact(ContactDTO contactDTO, @MappingTarget Contact contact);
}
